package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class AttendanceViewModel(private val repository: AttendanceRepository) : ViewModel() {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val humanDateFormatter = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())

    val allSemesters = repository.allSemesters.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeSemester = repository.activeSemester.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Configuration states
    val activeCourses = activeSemester.flatMapLatest { sem ->
        sem?.let { repository.getCoursesForSemester(it.id) } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeSemesterAllRecords = activeSemester.flatMapLatest { sem ->
        sem?.let { repository.getAllRecordsForSemester(it.id) } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Date selection
    private val _selectedDateCalendar = MutableStateFlow(Calendar.getInstance())
    val selectedDateCalendar: StateFlow<Calendar> = _selectedDateCalendar.asStateFlow()

    val selectedDateString = _selectedDateCalendar.map { dateFormatter.format(it.time) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), dateFormatter.format(Date()))

    val selectedDateLabel = _selectedDateCalendar.map { humanDateFormatter.format(it.time) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), humanDateFormatter.format(Date()))

    // Daily periods based on Timetable + Records
    val dailyPeriods: StateFlow<List<DayPeriodState>> = combine(
        selectedDateString,
        _selectedDateCalendar,
        activeSemester,
        activeSemesterAllRecords
    ) { dateStr, calendar, sem, allRecords ->
        if (sem == null) return@combine emptyList<DayPeriodState>()
        
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        // Need to fetch from DB instead of flow combining purely. 
        // We will collect and flatmap next
        emptyList<DayPeriodState>() // Placeholder, handled in flatMapLatest below
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Re-doing the daily periods correctly:
    val currentDayTimetable = combine(_selectedDateCalendar, activeSemester) { cal, sem ->
        Pair(cal.get(Calendar.DAY_OF_WEEK), sem?.id)
    }.flatMapLatest { (dayOfWeek, semId) ->
        if (semId != null) repository.getTimetableForDay(semId, dayOfWeek) else flowOf(emptyList())
    }

    val dayPeriods: StateFlow<List<DayPeriodState>> = combine(
        currentDayTimetable,
        activeSemesterAllRecords,
        selectedDateString
    ) { timetable, records, dateStr ->
        val dailyRecordsMap = records.filter { it.dateString == dateStr }.associateBy { it.periodIndex }

        timetable.map { tm ->
            val existingRecord = dailyRecordsMap[tm.entry.periodIndex]
            DayPeriodState(
                periodIndex = tm.entry.periodIndex,
                timeLabel = "${tm.entry.startTime} - ${tm.entry.endTime}",
                course = tm.course,
                status = existingRecord?.status ?: "UNMARKED"
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Timetable all
    val allTimetableEntries = activeSemester.flatMapLatest { sem ->
        sem?.let { repository.getAllTimetableEntries(it.id) } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // Stats
    val stats: StateFlow<AttendanceStats> = combine(activeSemesterAllRecords, activeCourses) { records, courses ->
        val validRecords = records.filter { it.status != "NONE" }
        
        val contentPresent = validRecords.count { it.status == "PRESENT" }
        val contentAbsent = validRecords.count { it.status == "ABSENT" }
        val overallTotal = contentPresent + contentAbsent
        val overallPercentage = if (overallTotal > 0) (contentPresent.toDouble() / overallTotal * 1000).toInt() / 10.0 else 0.0

        val subjectStatsList = courses.map { course ->
            val courseRecords = validRecords.filter { it.courseId == course.id }
            val present = courseRecords.count { it.status == "PRESENT" }
            val absent = courseRecords.count { it.status == "ABSENT" }
            val total = present + absent
            val percent = if (total > 0) (present.toDouble() / total * 1000).toInt() / 10.0 else 0.0

            val attendTo75 = if (percent < 75.0) kotlin.math.max(0, 3 * absent - present) else 0
            val canSkip = if (percent >= 75.0 && total > 0) kotlin.math.max(0, (present - 3 * absent) / 3) else 0

            SubjectAttendanceStats(
                course = course,
                presentCount = present,
                absentCount = absent,
                percentage = percent,
                attendTo75 = attendTo75,
                canSkip = canSkip
            )
        }

        AttendanceStats(
            overallPresent = contentPresent,
            overallAbsent = contentAbsent,
            overallPercentage = overallPercentage,
            subjectStats = subjectStatsList
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AttendanceStats())

    fun selectDate(date: Calendar) {
        val newCal = Calendar.getInstance().apply { timeInMillis = date.timeInMillis }
        _selectedDateCalendar.value = newCal
    }

    fun navigateDay(amount: Int) {
        val current = _selectedDateCalendar.value
        val newCal = Calendar.getInstance().apply {
            timeInMillis = current.timeInMillis
            add(Calendar.DAY_OF_YEAR, amount)
        }
        _selectedDateCalendar.value = newCal
    }

    fun selectToday() {
        _selectedDateCalendar.value = Calendar.getInstance()
    }

    fun markAttendance(periodIndex: Int, courseId: Long, status: String) {
        val semId = activeSemester.value?.id ?: return
        viewModelScope.launch {
            val dateStr = selectedDateString.value
            if (status == "UNMARKED") {
                repository.deleteRecord(semId, dateStr, periodIndex)
            } else {
                val record = AttendanceRecord(
                    semesterId = semId,
                    dateString = dateStr,
                    periodIndex = periodIndex,
                    courseId = courseId,
                    status = status
                )
                repository.insertRecord(record)
            }
        }
    }

    // Settings actions
    fun createAndActivateSemester(name: String) {
        viewModelScope.launch {
            val sid = repository.createSemester(name, activate = true)
            repository.setupDefaultTimetable(sid)
        }
    }

    fun activateSemester(id: Long) {
        viewModelScope.launch { repository.activateSemester(id) }
    }

    fun addCourse(name: String) {
        val semId = activeSemester.value?.id ?: return
        viewModelScope.launch {
            repository.insertCourse(Course(semesterId = semId, name = name))
        }
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch { repository.deleteCourse(course) }
    }

    fun updateTimetableEntry(entry: TimetableEntry, courseId: Long?) {
        viewModelScope.launch {
            repository.updateTimetableEntry(entry.copy(courseId = courseId))
        }
    }

    fun updateTimetablePeriodTimes(periodIndex: Int, startTime: String, endTime: String) {
        val semId = activeSemester.value?.id ?: return
        viewModelScope.launch {
            repository.updateTimetableTimesForPeriod(semId, periodIndex, startTime, endTime)
        }
    }
}

data class DayPeriodState(
    val periodIndex: Int,
    val timeLabel: String,
    val course: Course?,
    val status: String
)

data class SubjectAttendanceStats(
    val course: Course,
    val presentCount: Int,
    val absentCount: Int,
    val percentage: Double,
    val attendTo75: Int,
    val canSkip: Int
)

data class AttendanceStats(
    val overallPresent: Int = 0,
    val overallAbsent: Int = 0,
    val overallPercentage: Double = 0.0,
    val subjectStats: List<SubjectAttendanceStats> = emptyList()
)

class AttendanceViewModelFactory(private val repository: AttendanceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AttendanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AttendanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package com.example.data

import kotlinx.coroutines.flow.Flow

class AttendanceRepository(private val attendanceDao: AttendanceDao) {

    val activeSemester: Flow<Semester?> = attendanceDao.getActiveSemester()
    val allSemesters: Flow<List<Semester>> = attendanceDao.getAllSemesters()

    suspend fun createSemester(name: String, activate: Boolean = true): Long {
        if (activate) {
            attendanceDao.deactivateAllSemesters()
        }
        return attendanceDao.insertSemester(Semester(name = name, isActive = activate))
    }

    suspend fun activateSemester(semesterId: Long) {
        attendanceDao.deactivateAllSemesters()
        attendanceDao.activateSemester(semesterId)
    }

    fun getCoursesForSemester(semesterId: Long): Flow<List<Course>> {
        return attendanceDao.getCoursesForSemester(semesterId)
    }

    suspend fun insertCourse(course: Course): Long {
        return attendanceDao.insertCourse(course)
    }

    suspend fun deleteCourse(course: Course) {
        attendanceDao.deleteCourse(course)
    }

    fun getTimetableForDay(semesterId: Long, dayOfWeek: Int): Flow<List<TimetableEntryWithCourse>> {
        return attendanceDao.getTimetableForDay(semesterId, dayOfWeek)
    }
    
    fun getAllTimetableEntries(semesterId: Long): Flow<List<TimetableEntryWithCourse>> {
        return attendanceDao.getAllTimetableEntries(semesterId)
    }

    suspend fun setupDefaultTimetable(semesterId: Long, defaultStartTimeStr: String = "09:00", defaultEndTimeStr: String = "09:50") {
        val entries = mutableListOf<TimetableEntry>()
        for (day in 2..7) { // MONDAY(2) to SATURDAY(7)
            for (period in 1..8) {
                entries.add(
                    TimetableEntry(
                        semesterId = semesterId,
                        dayOfWeek = day,
                        periodIndex = period,
                        startTime = defaultStartTimeStr,
                        endTime = defaultEndTimeStr,
                        courseId = null
                    )
                )
            }
        }
        attendanceDao.insertTimetableEntries(entries)
    }

    suspend fun updateTimetableEntry(entry: TimetableEntry) {
        attendanceDao.updateTimetableEntry(entry)
    }

    suspend fun updateTimetableTimesForPeriod(semesterId: Long, periodIndex: Int, startTime: String, endTime: String) {
        attendanceDao.updateTimetableTimesForPeriod(semesterId, periodIndex, startTime, endTime)
    }

    fun getRecordsForDate(semesterId: Long, dateString: String): Flow<List<AttendanceRecord>> {
        return attendanceDao.getRecordsForDate(semesterId, dateString)
    }

    fun getAllRecordsForSemester(semesterId: Long): Flow<List<AttendanceRecord>> {
        return attendanceDao.getAllRecordsForSemester(semesterId)
    }

    suspend fun insertRecord(record: AttendanceRecord) {
        attendanceDao.insertRecord(record)
    }
    
    suspend fun deleteRecord(semesterId: Long, dateString: String, periodIndex: Int) {
        attendanceDao.deleteRecordByDetails(semesterId, dateString, periodIndex)
    }
}

package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Insert
    suspend fun insertSemester(semester: Semester): Long
    
    @Update
    suspend fun updateSemester(semester: Semester)

    @Query("SELECT * FROM semesters WHERE isActive = 1 LIMIT 1")
    fun getActiveSemester(): Flow<Semester?>

    @Query("SELECT * FROM semesters ORDER BY id DESC")
    fun getAllSemesters(): Flow<List<Semester>>

    @Query("UPDATE semesters SET isActive = 0")
    suspend fun deactivateAllSemesters()
    
    @Query("UPDATE semesters SET isActive = 1 WHERE id = :semesterId")
    suspend fun activateSemester(semesterId: Long)

    @Insert
    suspend fun insertCourse(course: Course): Long

    @Delete
    suspend fun deleteCourse(course: Course)

    @Query("SELECT * FROM courses WHERE semesterId = :semesterId")
    fun getCoursesForSemester(semesterId: Long): Flow<List<Course>>

    @Insert
    suspend fun insertTimetableEntries(entries: List<TimetableEntry>)
    
    @Update
    suspend fun updateTimetableEntry(entry: TimetableEntry)

    @Query("UPDATE timetable_entries SET startTime = :startTime, endTime = :endTime WHERE semesterId = :semesterId AND periodIndex = :periodIndex")
    suspend fun updateTimetableTimesForPeriod(semesterId: Long, periodIndex: Int, startTime: String, endTime: String)

    @Transaction
    @Query("SELECT * FROM timetable_entries WHERE semesterId = :semesterId AND dayOfWeek = :dayOfWeek ORDER BY periodIndex ASC")
    fun getTimetableForDay(semesterId: Long, dayOfWeek: Int): Flow<List<TimetableEntryWithCourse>>

    @Transaction
    @Query("SELECT * FROM timetable_entries WHERE semesterId = :semesterId ORDER BY dayOfWeek ASC, periodIndex ASC")
    fun getAllTimetableEntries(semesterId: Long): Flow<List<TimetableEntryWithCourse>>

    @Query("DELETE FROM timetable_entries WHERE semesterId = :semesterId")
    suspend fun clearTimetableForSemester(semesterId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceRecord)

    @Delete
    suspend fun deleteRecord(record: AttendanceRecord)

    @Query("SELECT * FROM attendance_records WHERE semesterId = :semesterId AND dateString = :dateString ORDER BY periodIndex ASC")
    fun getRecordsForDate(semesterId: Long, dateString: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE semesterId = :semesterId")
    fun getAllRecordsForSemester(semesterId: Long): Flow<List<AttendanceRecord>>
    
    @Query("DELETE FROM attendance_records WHERE dateString = :dateString AND periodIndex = :periodIndex AND semesterId = :semesterId")
    suspend fun deleteRecordByDetails(semesterId: Long, dateString: String, periodIndex: Int)
}

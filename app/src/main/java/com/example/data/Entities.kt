package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Embedded
import androidx.room.Relation

@Entity(tableName = "semesters")
data class Semester(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isActive: Boolean = false
)

@Entity(
    tableName = "courses",
    foreignKeys = [ForeignKey(entity = Semester::class, parentColumns = ["id"], childColumns = ["semesterId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("semesterId")]
)
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val semesterId: Long,
    val name: String
)

@Entity(
    tableName = "timetable_entries",
    foreignKeys = [
        ForeignKey(entity = Semester::class, parentColumns = ["id"], childColumns = ["semesterId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Course::class, parentColumns = ["id"], childColumns = ["courseId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("semesterId"), Index("courseId")]
)
data class TimetableEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val semesterId: Long,
    val dayOfWeek: Int,
    val periodIndex: Int,
    val startTime: String,
    val endTime: String,
    val courseId: Long?
)

@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(entity = Semester::class, parentColumns = ["id"], childColumns = ["semesterId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Course::class, parentColumns = ["id"], childColumns = ["courseId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("semesterId"), Index("courseId"), Index(value = ["dateString", "periodIndex", "semesterId"], unique = true)]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val semesterId: Long,
    val dateString: String,
    val periodIndex: Int,
    val courseId: Long,
    val status: String
)

data class TimetableEntryWithCourse(
    @Embedded val entry: TimetableEntry,
    @Relation(
        parentColumn = "courseId",
        entityColumn = "id"
    )
    val course: Course?
)

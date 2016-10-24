package datamodel

import ColumnDataMapper.{localDateTimeColumnType, setStringColumnType}
import java.sql.Timestamp
import java.time.LocalDateTime

import datamodel.DataModel.Task
import slick.driver.H2Driver.api._
import slick.lifted.ProvenShape

import scala.concurrent.Await
import scala.concurrent.duration.DurationLong

/**
  * Created by rajat on 24/10/16.
  */
object DataModel {

case class Task(
                   title: String,
                   description: String = "",
                   createdAt: LocalDateTime = LocalDateTime.now(),
                   dueBy: LocalDateTime,
                   tags: Set[String] = Set(),
                   id: Long = 0L)

  class TaskTable(tag: Tag) extends Table[Task](tag, "tasks") {
    def title = column[String]("title")

    def description = column[String]("description")

    def createdAt = column[LocalDateTime]("createdAt")(localDateTimeColumnType)

    def dueBy = column[LocalDateTime]("dueBy")(localDateTimeColumnType)

    def tags = column[Set[String]]("tags")(setStringColumnType)

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    override def * : ProvenShape[Task] = (title, description, createdAt, dueBy, tags, id) <>(Task.tupled, Task.unapply)
  }

  lazy val Tasks = TableQuery[TaskTable]

  val createTaskTableAction = Tasks.schema.create
  def insertTaskAction(tasks: Task*) = Tasks ++= tasks.toSeq
  val listTasksAction = Tasks.result
}

object ColumnDataMapper {

  implicit val localDateTimeColumnType = MappedColumnType.base[LocalDateTime, Timestamp](
    ldt => Timestamp.valueOf(ldt),
    t => t.toLocalDateTime
  )

  implicit val setStringColumnType = MappedColumnType.base[Set[String], String](
    tags => tags.mkString(","),
    tagsString => tagsString.split(",").toSet
  )

}

class CreateDatabaseSpec{

  val db = Database.forConfig("taskydb")

  def create() = {
    val result = Await.result(db.run(DataModel.createTaskTableAction), 2 seconds)
    println(result)
  }

  def add() = {
    val result = Await.result(db.run(DataModel.insertTaskAction(Task(title = "Learn Slick", dueBy = LocalDateTime.now().plusDays(1)))), 2 seconds)
    println(result)
  }

  def list() = {
    val result = Await.result(db.run(DataModel.listTasksAction), 2 seconds)
    print(result)
  }

}
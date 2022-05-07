package com.openAnimation.app

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.{MapSqlParameterSource, NamedParameterJdbcTemplate, SqlParameterSourceUtils}
import org.springframework.jdbc.datasource.SingleConnectionDataSource

import java.sql.{DriverManager, ResultSet}
import java.util
import scala.collection.JavaConverters._

class PrimaryDao() {

  def persistSnippetWithConn(snippetInMetadata: SnippetInMetadata): HttpResponse = {
    this.withConn(persistSnippet(_, snippetInMetadata, "open_animation", "Snippet"))
  }

  def getSnippetWithConn(snippetId: String): SnippetMetadata = {
    this.withConn(getSnippet(_, snippetId, "open_animation", "Snippet"))
  }

  def getAllSnippetsWithConn: List[SnippetMetadata] = {
    this.withConn(getAllSnippets(_, "open_animation", "Snippet"))
  }

  def persistSnippet(jdbcTemplate: NamedParameterJdbcTemplate, snippetInMetadata: SnippetInMetadata, schema: String, table: String): HttpResponse = {
    val upvote: Int = 0
    val downvote: Int = 0
    val insertSql =
      s"INSERT INTO $schema.$table (id, user, timeStart, timeEnd, duration, upvote, downvote)" +
      s"VALUES (:id, :user, :timeStart, :timeEnd, :duration, $upvote, $downvote)"
    val batchValues = SqlParameterSourceUtils.createBatch(snippetInMetadata)
    val persistCount: Array[Int] = jdbcTemplate.batchUpdate(insertSql, batchValues);
    val response = if (persistCount(0) > 0) HttpResponse(StatusCodes.OK, entity = "Snippet successfully persisted to database") else HttpResponse(500, entity = "Failed to persist snippet to database")
    response
  }

  def getSnippet(jdbcTemplate: NamedParameterJdbcTemplate, snippetId: String, schema: String, table: String): SnippetMetadata = {
    val selectSql = s"SELECT * FROM $schema.$table WHERE id=:id"
    val namedParameters = new MapSqlParameterSource().addValue("id", snippetId);
    val rowMapper = new RowMapper[SnippetMetadata] {
      override def mapRow(resultSet: ResultSet, row: Int): SnippetMetadata = {
        SnippetMetadata(
          resultSet.getString("id"),
          resultSet.getString("user"),
          resultSet.getDouble("timeStart"),
          resultSet.getDouble("timeEnd"),
          resultSet.getDouble("duration"),
          resultSet.getInt("upvote"),
          resultSet.getInt("downvote")
        )
      }
    }
    val snippetMetadata = jdbcTemplate.queryForObject(selectSql, namedParameters, rowMapper)
    snippetMetadata
  }

  def getAllSnippets(jdbcTemplate: NamedParameterJdbcTemplate, schema: String, table: String): List[SnippetMetadata] = {
    val selectSql = s"SELECT * FROM $schema.$table"
    val rowMapper = new RowMapper[SnippetMetadata] {
      override def mapRow(resultSet: ResultSet, row: Int): SnippetMetadata = {
        SnippetMetadata(
          resultSet.getString("id"),
          resultSet.getString("user"),
          resultSet.getDouble("timeStart"),
          resultSet.getDouble("timeEnd"),
          resultSet.getDouble("duration"),
          resultSet.getInt("upvote"),
          resultSet.getInt("downvote")
        )
      }
    }
    val snippetMetadataList: List[SnippetMetadata] = jdbcTemplate.query(selectSql, rowMapper).asScala.toList
    snippetMetadataList
  }

  def withConn[T](func: NamedParameterJdbcTemplate => T): T = {
    val driver = "com.mysql.cj.jdbc.Driver"
    val url = "jdbc:mysql://localhost:3306/open_animation"
    val name = "guest"
    val pass = ""
    Class.forName(driver)
    val connection = DriverManager.getConnection(url, name, pass)
    try {
      val jdbcTemplate = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(connection, true))
      func(jdbcTemplate)
    }
    catch {
      case e: Throwable => throw e
    }
    finally {
      connection.close()
    }
  }

}

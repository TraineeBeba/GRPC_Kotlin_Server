package org.example

import io.grpc.Grpc
import io.grpc.InsecureServerCredentials
import io.grpc.Server
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.example.component.Row
import org.example.component.Table
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

class HelloWorldServer {
    private var server: Server? = null

    @Throws(IOException::class)
    private fun start() {
        val port = 50051
        server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
            .addService(GreeterImpl())
            .build()
            .start()
        logger.info("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                println("*** shutting down gRPC server since JVM is shutting down")
                try {
                    this@HelloWorldServer.stop()
                } catch (e: InterruptedException) {
                    e.printStackTrace(System.err)
                }
                println("*** server shut down")
            }
        })
    }

    @Throws(InterruptedException::class)
    private fun stop() {
        server?.shutdown()?.awaitTermination(30, TimeUnit.SECONDS)
    }

    @Throws(InterruptedException::class)
    private fun blockUntilShutdown() {
        server?.awaitTermination()
    }

    companion object {
        private val logger = Logger.getLogger(HelloWorldServer::class.java.name)
        lateinit var dbManager: DatabaseManager

        @JvmStatic
        @Throws(IOException::class, InterruptedException::class)
        fun main(args: Array<String>) {
            val server = HelloWorldServer()
            dbManager = DatabaseManager.getInstance()
            dbManager.createDB("DB")
            dbManager.populateTable()
            dbManager.populateTable()
            server.start()
            server.blockUntilShutdown()
        }
    }

    class GreeterImpl : RemoteDBGrpc.RemoteDBImplBase() {
        override fun getRows(request: GetRowsRequest, responseObserver: StreamObserver<GetRowsResponse>) {
            val tableIndex = request.tableIndex
            val rows = ArrayList<org.example.Row>()
            val DBrows = dbManager.database!!.tables[tableIndex].rows
            for (row in DBrows) {
                rows.add(org.example.Row.newBuilder().addAllValues(row.values).build())
            }

            val responseBuilder = GetRowsResponse.newBuilder()
            for (row in rows) {
                responseBuilder.addRows(row)
            }
            responseObserver.onNext(responseBuilder.build())
            responseObserver.onCompleted()
        }

        override fun getColumns(request: GetColumnsRequest, responseObserver: StreamObserver<GetColumnsResponse>) {
            val tableIndex = request.tableIndex
            val columns = ArrayList<Column>()
            val DBcolumns = dbManager.database!!.tables[tableIndex].columns
            for (column in DBcolumns) {
                columns.add(Column.newBuilder().setName(column.name).setType(ColumnType.valueOf(column.type)).build())
            }

            val responseBuilder = GetColumnsResponse.newBuilder()
            for (column in columns) {
                responseBuilder.addColumns(column)
            }
            responseObserver.onNext(responseBuilder.build())
            responseObserver.onCompleted()
        }

        override fun getTablesData(request: GetTablesDataRequest, responseObserver: StreamObserver<GetTablesDataResponse>) {
            val tables = dbManager.database!!.tables
            val responseBuilder = GetTablesDataResponse.newBuilder()

            for (table in tables) {
                val tableData = TableData.newBuilder().setName(table.name).setIndex(tables.indexOf(table)).build()
                responseBuilder.addTablesData(tableData)
            }
            responseObserver.onNext(responseBuilder.build())
            responseObserver.onCompleted()
        }

        override fun createTable(request: CreateTableRequest, responseObserver: StreamObserver<CreateTableResponse>) {
            val name = request.name
            val success = dbManager.addTable(name)
            val response = CreateTableResponse.newBuilder().setSuccess(success).build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        }

        override fun addRow(request: AddRowRequest, responseObserver: StreamObserver<AddRowResponse>) {
            val tableIndex = request.tableIndex
            val success = dbManager.addRow(tableIndex, org.example.component.Row())
            val response = AddRowResponse.newBuilder().setSuccess(success).build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        }

        override fun addColumn(request: AddColumnRequest, responseObserver: StreamObserver<AddColumnResponse>) {
            val tableIndex = request.tableIndex
            val name = request.name
            val columnType = request.columnType
            val min = request.min
            val max = request.max

            val success = if (columnType == ColumnType.MONEY_INVL) {
                dbManager.addColumn(tableIndex, name, org.example.component.column.ColumnType.valueOf(columnType.name), min, max)
            } else {
                dbManager.addColumn(tableIndex, name, org.example.component.column.ColumnType.valueOf(columnType.name))
            }

            val response = AddColumnResponse.newBuilder().setSuccess(success).build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        }

        override fun deleteTable(request: DeleteTableRequest, responseObserver: StreamObserver<DeleteTableResponse>) {
            val tableIndex = request.tableIndex
            val success = dbManager.deleteTable(tableIndex)
            val response = DeleteTableResponse.newBuilder().setSuccess(success).build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        }

        override fun deleteColumn(request: DeleteColumnRequest, responseObserver: StreamObserver<DeleteColumnResponse>) {
            val tableIndex = request.tableIndex
            val columnIndex = request.columnIndex
            val success = dbManager.deleteColumn(tableIndex, columnIndex)
            val response = DeleteColumnResponse.newBuilder().setSuccess(success).build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        }

        override fun deleteRow(request: DeleteRowRequest, responseObserver: StreamObserver<DeleteRowResponse>) {
            val tableIndex = request.tableIndex
            val rowIndex = request.rowIndex
            val success = dbManager.deleteRow(tableIndex, rowIndex)
            val response = DeleteRowResponse.newBuilder().setSuccess(success).build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        }

        override fun changeColumnType(request: ChangeColumnTypeRequest, responseObserver: StreamObserver<ChangeColumnTypeResponse>) {
            val tableIndex = request.tableIndex
            val columnIndex = request.columnIndex
            val columnType = request.columnType
            val success = dbManager.changeColumnType(tableIndex, columnIndex, org.example.component.column.ColumnType.valueOf(columnType.name))
            val response = ChangeColumnTypeResponse.newBuilder().setSuccess(success).build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        }

        override fun editCell(request: EditCellRequest, responseObserver: StreamObserver<EditCellResponse>) {
            val tableIndex = request.tableIndex
            val rowIndex = request.rowIndex
            val columnIndex = request.columnIndex
            val newValue = request.value
            val success = dbManager.updateCellValue(newValue, tableIndex, columnIndex, rowIndex)
            val response = EditCellResponse.newBuilder().setSuccess(success).build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        }

        override fun createTestTable(request: CreateTestTableRequest, responseObserver: StreamObserver<CreateTestTableResponse>) {
            try {
                dbManager.populateTable()
                val response = CreateTestTableResponse.newBuilder().setSuccess(true).build()
                responseObserver.onNext(response)
            } catch (e: Exception) {
                responseObserver.onError(Status.INTERNAL.withDescription(e.message).asRuntimeException())
            } finally {
                responseObserver.onCompleted()
            }
        }

        override fun deleteDuplicateRows(request: DeleteDuplicateRowsRequest, responseObserver: StreamObserver<DeleteDuplicateRowsResponse>) {
            val tableIndex = request.tableIndex
            val success = dbManager.deleteDuplicateRows(tableIndex)
            val response = DeleteDuplicateRowsResponse.newBuilder().setSuccess(success).build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        }
    }
}

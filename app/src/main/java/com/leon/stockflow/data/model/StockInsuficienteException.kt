package com.leon.stockflow.data.model

/**
 * Se lanza si una SALIDA pide más unidades de las que hay en stock.
 */
class StockInsuficienteException(val idProducto: Long) :
    Exception("Stock insuficiente para el producto $idProducto")
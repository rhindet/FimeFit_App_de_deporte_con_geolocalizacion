package com.arrap.fimefit

data class Totals(
    var recordAvgSpeed: Double ?= null,
    var recordDistance: Double ?= null,
    var recordSpeed: Double ?= null,
    var totalDistance: Double ?= null,
    var totalRuns: Int ?= null,
    var totalTime: Int ?= null
)

package com.arrap.fimefit

data class Location(
    var hasAltitude: Boolean ?= null,
    var altitude: Double ?= null,
    var latitude: Double ?= null,
    var longitude: Double ?= null,

    var color: Int ?= null,
    var maxSpeed: Boolean?= null,
    var speedFromMe: Double ?= null,
    var speedFromLocation: Double ?= null,

    var time: String ?= null



)

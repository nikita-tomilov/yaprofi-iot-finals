//package com.nikitatomilov.yaprofiiotbackends.controllers
//
//import com.nikitatomilov.yaprofiiotbackends.services.HelicopterDevice
//import org.springframework.http.MediaType
//import org.springframework.web.bind.annotation.GetMapping
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.bind.annotation.RestController
//
//@RestController
//@RequestMapping(value = ["/api/1.0/helicopter/"], produces = [MediaType.APPLICATION_JSON_VALUE])
//class HelicopterDeviceController(
//  private val helicopterDevice: HelicopterDevice
//) {
//
//  @GetMapping("/buzzer/on")
//  fun buzzerOn(): String {
//    helicopterDevice.buzzerOn()
//    return "ok"
//  }
//
//  @GetMapping("/buzzer/off")
//  fun buzzerOff(): String {
//    helicopterDevice.buzzerOff()
//    return "ok"
//  }
//}
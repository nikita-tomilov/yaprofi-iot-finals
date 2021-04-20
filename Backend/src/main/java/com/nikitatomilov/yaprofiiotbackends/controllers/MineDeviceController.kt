//package com.nikitatomilov.yaprofiiotbackends.controllers
//
//import com.nikitatomilov.yaprofiiotbackends.services.MineDevice
//import org.springframework.http.MediaType
//import org.springframework.web.bind.annotation.GetMapping
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.bind.annotation.RestController
//
//@RestController
//@RequestMapping(value = ["/api/1.0/mine/"], produces = [MediaType.APPLICATION_JSON_VALUE])
//class MineDeviceController(
//  private val mineDevice: MineDevice
//) {
//
//  @GetMapping("/buzzer/on")
//  fun buzzerOn(): String {
//    mineDevice.buzzerOn()
//    return "ok"
//  }
//
//  @GetMapping("/buzzer/off")
//  fun buzzerOff(): String {
//    mineDevice.buzzerOff()
//    return "ok"
//  }
//
//  @GetMapping("/vent/on")
//  fun ventOn(): String {
//    mineDevice.ventOn()
//    return "ok"
//  }
//
//  @GetMapping("/vent/off")
//  fun ventOff(): String {
//    mineDevice.ventOff()
//    return "ok"
//  }
//
//}
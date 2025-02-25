//package com.talk_space.api.controller.publics;
//
//import com.talk_space.exceptions.CustomExceptions;
//import com.talk_space.model.domain.User;
//import com.talk_space.model.dto.ForgotPassword;
//import com.talk_space.service.MailSenderService;
//import com.talk_space.service.UserService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//
//@RestController
//@RequestMapping("/api/public/email")
//@RequiredArgsConstructor
//public class PublicMailSenderController {
//
//    private final MailSenderService mailSenderService;
//
//    @PutMapping("/pin")
//    public ResponseEntity<String> savePin(@RequestParam String email) {
//        try {
//            String responseMessage = mailSenderService.handlePinRequest(email, true);
//            return ResponseEntity.ok(responseMessage);
//        } catch (CustomExceptions.UserNotFoundException | CustomExceptions.UserNotActiveException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(e.getMessage());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("An unexpected error occurred.");
//        }
//    }
//
//
////    @PutMapping("/active")
////    public ResponseEntity<?> savePinForActive(@RequestParam String mail) {
////        try {
////            String responseMessage = mailSenderService.handlePinRequest(mail, false);
////            return ResponseEntity.ok(responseMessage);
////        } catch (CustomExceptions.UserNotFoundException | CustomExceptions.UserNotActiveException e) {
////            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
////                    .body(e.getMessage());
////        } catch (Exception e) {
////            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
////                    .body("An unexpected error occurred.");
////        }
////    }
//
//
//}

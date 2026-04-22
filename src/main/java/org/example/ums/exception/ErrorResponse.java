package org.example.ums.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@AllArgsConstructor
public class ErrorResponse {
    String error;
    String desc;
}

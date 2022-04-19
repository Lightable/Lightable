package com.feuer.chatty.decoders.Exceptions;

import java.io.IOException;

public class CEFormatException extends IOException {
    static final long serialVersionUID = -7139121221067081482L;
    public CEFormatException(String s) {
        super(s);
    }
}

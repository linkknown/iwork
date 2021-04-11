package com.linkknown.iwork.core.func.lexerv2;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TokenEntity {

    private String tokenString;
    private String tokenRegex;
    private TokenEnum tokenEnum;
}

package org.example.lfe;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class ObjectBox {

    int x;
    int y;
    int h;
    int w;
    int z1;
    int z2;
    //TODO: criar default value for z1 and z2

}

package pl.jakubkonkol.tasteitserver.badgeHandler;


import pl.jakubkonkol.tasteitserver.model.enums.BadgeType;

import java.lang.annotation.Repeatable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(TrackBadgesProcesses.class)
public @interface TrackBadgesProgress {
//    String[] badgeId(); //todo wybrac to i przerobic reszte
    BadgeType[] badges();
}

package com.chague.bibliotheque.domain;

/*
3 choix de langue c'est suffisant pour une application à vocation de démonstration.
En théorie, on pourrait faire ça sans avoir recours à une énumération.
Mais c'est plus propre, surtout si on veut scaler.
 */
public enum Langue {
    FR,
    EN,
    ES
}

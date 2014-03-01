twitter4dav[![Build Status](https://travis-ci.org/ChristianHoesel/twitter4dav.png?branch=master)](https://travis-ci.org/ChristianHoesel/twitter4dav)
===========

Experimentelles Plugin für das ERZ Datenverteiler Rahmenwerk zum twittern von Baustellen, Staus, Unfällen usw..

####Hinweis zur Verwendung
Für die Authentifiktion bei Twitter wird OAuth verwendet. D.h. die Anwendung wird bei Twitter registriert (https://apps.twitter.com/app/new) und erhält einen sog. Consumer Key und ein Consumer Secret, quasi Name und Passwort der Applikation. Diese fehlen in den eingecheckten Quellen und sollten sich in der Properties Datei "twitter.properties" im Package 
```
de.hoesel.dav.buv.twitter.internal
```
befinden.

Beispiel für twitter.properties:

```
OAuthConsumerKey=1234566789ABC
OAuthConsumerSecret=abcdefghijklmnopqrstuvwxyz
```

======================


Dieses Projekt ist nicht Teil des NERZ e.V. Die offizielle Software sowie weitere Informationen zur bundeseinheitlichen Software für Verkehrsrechnerzentralen (BSVRZ) finden Sie unter http://www.nerz-ev.de.

Wireless Signal Strength Diagnostics
====================================
This app shows the signal strength and other info for a chosen Wi-Fi access point.

Simply enter the name of a Wi-Fi access point (you can use * for wildcards), and press 'Go' to start a scan. Once the scan completes, you'll see various information about the matched Wi-Fi network(s), grouped by band/BSSID. You can also perform multiple scans to get a more accurate reading, by calculating the average (mean), median, min or max of the collected signal strength levels.

Created for use at Northeastern University.

The Internet permission is used for Crashlytics bug reports.

![screenshot](http://i.imgur.com/G2TXk0W.png)&nbsp;&nbsp;&nbsp;&nbsp;![screenshot](http://i.imgur.com/FZwNylG.png)

Download
--------
This app is published in [Google Play](https://play.google.com/store/apps/details?id=edu.neu.rrc.wifisigstrdiag).

Setup
-----
Make sure to create a `signing.properties` file in the root directory, containing the `STORE_FILE`, `STORE_PASSWORD`, `KEY_ALIAS` and `KEY_PASSWORD` properties.

This app is best developed with the latest version of Android Studio and gradle. To build & install a signed APK, run `gradlew build` and `gradlew installRelease`.

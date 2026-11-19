p = "android/app/src/main/AndroidManifest.xml"
s = open(p, encoding="utf-8").read()
perms = [
 "ACCESS_FINE_LOCATION","ACCESS_COARSE_LOCATION","ACCESS_BACKGROUND_LOCATION",
 "FOREGROUND_SERVICE","FOREGROUND_SERVICE_LOCATION","POST_NOTIFICATIONS",
 "REQUEST_IGNORE_BATTERY_OPTIMIZATIONS","WAKE_LOCK","INTERNET"]
add = ""
for x in perms:
    tag = 'android.permission.' + x
    if tag not in s:
        add += '    <uses-permission android:name="%s" />\n' % tag
if add:
    s = s.replace("<application", add + "    <application", 1)
svc = '        <service android:name=".TrackerService" android:exported="false" android:foregroundServiceType="location" />\n'
if ".TrackerService" not in s:
    s = s.replace("</application>", svc + "    </application>", 1)
open(p, "w", encoding="utf-8").write(s)

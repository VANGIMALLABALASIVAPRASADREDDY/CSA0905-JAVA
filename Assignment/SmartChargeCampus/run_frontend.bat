@echo off
echo ============================================================
echo Starting SmartCharge Campus Java Swing Desktop Frontend
echo ============================================================
cd frontend
"C:\Users\moham\.m2\apache-maven-3.9.6\bin\mvn.cmd" compile exec:java -Dexec.mainClass="com.smartcharge.client.Main"
pause

@echo off
setlocal EnableDelayedExpansion
echo ***************************************************************************
echo            compile_packages.bat
echo                     by niuren.zhu
echo                           2017.12.05
echo  说明：
echo     1. 安装apache-maven，下载地址http://maven.apache.org/download.cgi。
echo     2. 解压apache-maven，并设置系统变量MAVEN_HOME为解压的程序目录。
echo     3. 添加PATH变量到%%MAVEN_HOME%%\bin，并检查JAVA_HOME配置是否正确。
echo     4. 运行提示符运行mvn -v 检查安装是否成功。
echo     5. 此脚本用于编译测试war包。
echo ****************************************************************************
REM 设置参数变量
SET WORK_FOLDER=%~dp0

echo --当前工作的目录是[%WORK_FOLDER%]
if exist %WORK_FOLDER%release\ rd /s /q %WORK_FOLDER%release\
if not exist %WORK_FOLDER%release md %WORK_FOLDER%release
REM 编译测试包
if exist %WORK_FOLDER%ibas.integration.service\pom.test.xml (
  call "%MAVEN_HOME%\bin\mvn" clean package -Dmaven.test.skip=true -f %WORK_FOLDER%ibas.integration.service\pom.test.xml 
  if exist %WORK_FOLDER%ibas.integration.service\target\ibas.integration.test*.war copy /y %WORK_FOLDER%ibas.integration.service\target\ibas.integration.test*.war %WORK_FOLDER%release
)


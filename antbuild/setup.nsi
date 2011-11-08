# OntoVCS user-mode installer

Name OntoVCS

RequestExecutionLevel user

# General Symbol Definitions
!define REGKEY "SOFTWARE\$(^Name)"
!define VERSION @build.version@
!define COMPANY "Tomsk Polytechnic University"
!define URL ""

# Included files
!include Sections.nsh
!include LogicLib.nsh
!include EnvVarUpdate.nsh

# Reserved Files
ReserveFile "${NSISDIR}\Plugins\StartMenu.dll"

# Variables
Var StartMenuGroup

# Installer pages
Page license
Page directory
Page custom StartMenuGroupSelect "" ": Start Menu Folder"
Page instfiles

# Installer attributes
#!define MUI_ICON ..\..\logo\ontovcs.ico
OutFile OntoVCS-@build.version@.exe
InstallDir $PROFILE\OntoVCS
CRCCheck on
XPStyle on
Icon ..\..\logo\ontovcs.ico
ShowInstDetails show
AutoCloseWindow false
LicenseData ..\..\root\license.txt
VIProductVersion 0.@build.version@
VIAddVersionKey ProductName OntoVCS
VIAddVersionKey ProductVersion "${VERSION}"
VIAddVersionKey CompanyName "${COMPANY}"
VIAddVersionKey FileVersion "${VERSION}"
VIAddVersionKey FileDescription ""
VIAddVersionKey LegalCopyright ""
InstallDirRegKey HKCU "${REGKEY}" Path
UninstallIcon "${NSISDIR}\Contrib\Graphics\Icons\classic-uninstall.ico"
ShowUninstDetails show

# Installer sections
!macro CREATE_SMGROUP_SHORTCUT NAME PATH
    Push "${NAME}"
    Push "${PATH}"
    Call CreateSMGroupShortcut
!macroend

Section -OntoVCS SEC0000
    SetOutPath $INSTDIR
    SetOverwrite on
    File /r ontovcs\*
    SetOutPath $INSTDIR\lib\swt
    File /r ..\swt\*
    WriteRegStr HKCU "${REGKEY}\Components" OntoVCS 1
    ${EnvVarUpdate} $0 "PATH" "A" "HKCU" $INSTDIR\bin

    ExecCmd::exec 'java -d64 -version' ''
    Pop $0 ; return value - process exit code or error or STILL_ACTIVE (0x103).
    ${If} $0 == 0
        CopyFiles /SILENT $INSTDIR\lib\swt\x64\swt.jar $INSTDIR\lib\swt
    ${Else}
        CopyFiles /SILENT $INSTDIR\lib\swt\x86\swt.jar $INSTDIR\lib\swt
    ${EndIf}
SectionEnd

Section "Main Menu Shortcuts" SEC0001
    !insertmacro CREATE_SMGROUP_SHORTCUT "OntoVCS Tutorial" http://code.google.com/p/ontovcs/wiki/Tutorial
    !insertmacro CREATE_SMGROUP_SHORTCUT Configuration http://code.google.com/p/ontovcs/wiki/RepositoryConfiguration
    WriteRegStr HKCU "${REGKEY}\Components" "Main Menu Shortcuts" 1
SectionEnd

Section -post SEC0002
    WriteRegStr HKCU "${REGKEY}" Path $INSTDIR
    WriteRegStr HKCU "${REGKEY}" StartMenuGroup $StartMenuGroup
    SetOutPath $INSTDIR
    WriteUninstaller $INSTDIR\uninstall.exe
    !insertmacro CREATE_SMGROUP_SHORTCUT "Uninstall $(^Name)" $INSTDIR\uninstall.exe
    WriteRegStr HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayName "$(^Name)"
    WriteRegStr HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayVersion "${VERSION}"
    WriteRegStr HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" Publisher "${COMPANY}"
    WriteRegStr HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayIcon $INSTDIR\uninstall.exe
    WriteRegStr HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" UninstallString $INSTDIR\uninstall.exe
    WriteRegDWORD HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" NoModify 1
    WriteRegDWORD HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" NoRepair 1
SectionEnd

# Macro for selecting uninstaller sections
!macro SELECT_UNSECTION SECTION_NAME UNSECTION_ID
    Push $R0
    ReadRegStr $R0 HKCU "${REGKEY}\Components" "${SECTION_NAME}"
    StrCmp $R0 1 0 next${UNSECTION_ID}
    !insertmacro SelectSection "${UNSECTION_ID}"
    GoTo done${UNSECTION_ID}
next${UNSECTION_ID}:
    !insertmacro UnselectSection "${UNSECTION_ID}"
done${UNSECTION_ID}:
    Pop $R0
!macroend

# Uninstaller sections
!macro DELETE_SMGROUP_SHORTCUT NAME
    Push "${NAME}"
    Call un.DeleteSMGroupShortcut
!macroend

Section /o "-un.Main Menu Shortcuts" UNSEC0001
    !insertmacro DELETE_SMGROUP_SHORTCUT Configuration
    !insertmacro DELETE_SMGROUP_SHORTCUT "OntoVCS Tutorial"
    DeleteRegValue HKCU "${REGKEY}\Components" "Main Menu Shortcuts"
SectionEnd

Section /o -un.OntoVCS UNSEC0000
    RmDir /r /REBOOTOK $INSTDIR\lib\swt
    RmDir /r /REBOOTOK $INSTDIR
    DeleteRegValue HKCU "${REGKEY}\Components" OntoVCS
    ${un.EnvVarUpdate} $0 "PATH" "R" "HKCU" $INSTDIR\bin
SectionEnd

Section -un.post UNSEC0002
    DeleteRegKey HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)"
    !insertmacro DELETE_SMGROUP_SHORTCUT "Uninstall $(^Name)"
    Delete /REBOOTOK $INSTDIR\uninstall.exe
    DeleteRegValue HKCU "${REGKEY}" StartMenuGroup
    DeleteRegValue HKCU "${REGKEY}" Path
    DeleteRegKey /IfEmpty HKCU "${REGKEY}\Components"
    DeleteRegKey /IfEmpty HKCU "${REGKEY}"
    RmDir /REBOOTOK $SMPROGRAMS\$StartMenuGroup
    RmDir /REBOOTOK $INSTDIR
    Push $R0
    StrCpy $R0 $StartMenuGroup 1
    StrCmp $R0 ">" no_smgroup
no_smgroup:
    Pop $R0
SectionEnd

# Installer functions
Function StartMenuGroupSelect
    Push $R1
    StartMenu::Select /checknoshortcuts "Do not create shortcuts" /autoadd /text "Select the Start Menu folder in which to create the program's shortcuts:" /lastused $StartMenuGroup OntoVCS
    Pop $R1
    StrCmp $R1 success success
    StrCmp $R1 cancel done
    MessageBox MB_OK $R1
    Goto done
success:
    Pop $StartMenuGroup
done:
    Pop $R1
FunctionEnd

Function .onInit
    InitPluginsDir
    ExecCmd::exec 'java -version' ''
    Pop $0 ; return value - process exit code or error or STILL_ACTIVE (0x103).
    ${If} $0 != 0
        MessageBox MB_YESNO|MB_ICONEXCLAMATION \
            "Java is not found in PATH. $\n$\n\
            Please ensure that you can run java from command prompt before running the installer. $\n$\n\
            If you continue now, the installer will not be able to identify \
            the version of SWT correctly, so you will have to do it manually. $\n$\n\
            Are you sure you want to continue without Java?" \
            IDYES continue
        Abort
    continue:
        MessageBox MB_OK|MB_ICONINFORMATION \
            "Please read $INSTDIR\lib\swt\readme.txt for the instructions on installing SWT."
    ${EndIf}
FunctionEnd

Function CreateSMGroupShortcut
    Exch $R0 ;PATH
    Exch
    Exch $R1 ;NAME
    Push $R2
    StrCpy $R2 $StartMenuGroup 1
    StrCmp $R2 ">" no_smgroup
    SetOutPath $SMPROGRAMS\$StartMenuGroup
    CreateShortcut "$SMPROGRAMS\$StartMenuGroup\$R1.lnk" $R0
no_smgroup:
    Pop $R2
    Pop $R1
    Pop $R0
FunctionEnd

# Uninstaller functions
Function un.onInit
    ReadRegStr $INSTDIR HKCU "${REGKEY}" Path
    ReadRegStr $StartMenuGroup HKCU "${REGKEY}" StartMenuGroup
    !insertmacro SELECT_UNSECTION OntoVCS ${UNSEC0000}
    !insertmacro SELECT_UNSECTION "Main Menu Shortcuts" ${UNSEC0001}
FunctionEnd

Function un.DeleteSMGroupShortcut
    Exch $R1 ;NAME
    Push $R2
    StrCpy $R2 $StartMenuGroup 1
    StrCmp $R2 ">" no_smgroup
    Delete /REBOOTOK "$SMPROGRAMS\$StartMenuGroup\$R1.lnk"
no_smgroup:
    Pop $R2
    Pop $R1
FunctionEnd


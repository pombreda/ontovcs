#define MyAppName "OntoVCS"
#define MyAppVersion "@build.version@"
#define MyAppPublisher "Tomsk Polytechnic University"
#define MyAppURL "http://code.google.com/p/ontovcs"
#define PathKey "SYSTEM\CurrentControlSet\Control\Session Manager\Environment"

[Setup]
AppId={{89195FA0-30B2-470E-8FBC-F18D72F5F3FE}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppVerName={#MyAppName} {#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={pf}\ontovcs
DefaultGroupName={#MyAppName}
AllowNoIcons=yes
LicenseFile=ontovcs\license.txt
OutputBaseFilename=OntoVCS-{#MyAppVersion}
SetupIconFile=..\..\logo\ontovcs.ico
Compression=lzma
SolidCompression=yes
ChangesEnvironment=yes

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Files]
Source: "ontovcs\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "..\swt\*"; DestDir: "{app}\lib\swt"; Flags: nocompression ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\{cm:ProgramOnTheWeb,{#MyAppName}}"; Filename: "{#MyAppURL}"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"

[Registry]
Root: HKLM; Subkey: "{#PathKey}"; ValueType: expandsz; ValueName: "Path"; ValueData: "{olddata};{app}\bin"; Check: NeedsAddPath('{app}\bin')

[Code]

procedure Explode(var Dest: TArrayOfString; Text: String; Separator: String);
var
    i: Integer;
begin
  i := 0;
  repeat
    SetArrayLength(Dest, i+1);
    if Pos(Separator,Text) > 0 then
    begin
      Dest[i] := Copy(Text, 1, Pos(Separator, Text)-1);
      Text := Copy(Text, Pos(Separator,Text) + Length(Separator), Length(Text));
      i := i + 1;
    end else
    begin
      Dest[i] := Text;
      Text := '';
    end;
  until Length(Text) = 0;
end;

function Join(List: TArrayOfString; Separator: String): String;
var
  I: Integer;
begin
  Result := '';
  for I := Low(List) to High(List) - 1 do
  begin
    Result := Result + List[I] + Separator;
  end;  
  Result := Result + List[High(List)];
end;

function NeedsAddPath(Param: string): boolean;
var
  OrigPath: string;
begin
  if not RegQueryStringValue(HKEY_LOCAL_MACHINE,
    '{#PathKey}', 'Path', OrigPath) then
  begin
    Result := True;
    Exit;
  end;
  // look for the path with leading and trailing semicolon
  // Pos() returns 0 if not found
  Result := Pos(';' + Param + ';', ';' + OrigPath + ';') = 0;
end;

procedure DecodeVersion (verstr: String; var verint: array of Integer);
var
  i, p: Integer;
  S: String;
begin
  // initialize array
  verint := [0,0,0,0];
  i := 0;
  while ((Length(verstr) > 0) and (i < 4)) do
  begin
    p := pos ('.', verstr);
    if p > 0 then
    begin
      if p = 1 then s:= '0' else s:= Copy (verstr, 1, p - 1);
      verint[i] := StrToInt(s);
      i := i + 1;
      verstr := Copy (verstr, p+1, Length(verstr));
    end
    else
    begin
      verint[i] := StrToInt (verstr);
      verstr := '';
    end;
  end;
end;

function CompareVersion (ver1, ver2: String): Integer;
var
  verint1, verint2: array of Integer;
  i: Integer;
begin
  SetArrayLength (verint1, 4);
  DecodeVersion (ver1, verint1);

  SetArrayLength (verint2, 4);
  DecodeVersion (ver2, verint2);

  Result := 0;
  i := 0;
  while ((Result = 0) and ( i < 4 )) do
  begin
    if verint1[i] > verint2[i] then
      Result := 1
    else
      if verint1[i] < verint2[i] then
        Result := -1
      else
        Result := 0;
    i := i + 1;
  end;
end;

function IsJavaInPath(): Boolean;
var
  ErrorCode: Integer;
begin
  Result := ShellExec('', 'java', '-version', '', SW_HIDE, ewWaitUntilTerminated, ErrorCode);
end;

function IsJava64(): Boolean;
var
  ErrorCode: Integer;
begin
  Result := ShellExec('', 'java', '-d64 -version', '', SW_HIDE, ewWaitUntilTerminated, ErrorCode);
end;

function InitializeSetup(): Boolean;
var
  ErrorCode: Integer;
  JavaVer : String;
  MsgResult: Integer;
begin
  Result := False;
  RegQueryStringValue(HKLM, 'SOFTWARE\JavaSoft\Java Runtime Environment', 'CurrentVersion', JavaVer);
  if (Length(JavaVer) > 0) and (CompareVersion(JavaVer, '1.6') >= 0) then
  begin
    if IsJavaInPath() then
    begin
      Result := True;
      Exit;
    end else
    begin
      MsgResult := MsgBox('{#MyAppName} requires java executable in PATH. Please add it to PATH and run this setup again.' + #13 + #10 + 'Do you want to do it now? If so, System Properties windows will be opened.',
      mbConfirmation, MB_YESNO);
      if MsgResult = idYes then
      begin
      ShellExec('open', 'sysdm.cpl',
        '','', SW_SHOWNORMAL, ewNoWait, ErrorCode);
      end;
    end;
  end else
  begin
    MsgResult := MsgBox('{#MyAppName} requires Java Runtime Environment v1.6 or higher. Please download and install JRE and run this setup again.' + #13 + #10 + 'Do you want to download it now?',
      mbConfirmation, MB_YESNO);
    if MsgResult = idYes then
    begin
      ShellExec('open',
        'http://www.java.com/en/download/manual.jsp#win',
        '','', SW_SHOWNORMAL, ewNoWait, ErrorCode);
    end;
  end;
end;

procedure CurStepChanged(CurStep: TSetupStep);
begin
  if ssPostInstall = CurStep then
  begin
    DeleteFile(ExpandConstant('{app}\setup.cmd'));
    if IsJava64() then
    begin
      FileCopy(ExpandConstant('{app}\lib\swt\x64\swt.jar'), 
        ExpandConstant('{app}\lib\swt\swt.jar'), False);
    end else
    begin
      FileCopy(ExpandConstant('{app}\lib\swt\x86\swt.jar'), 
        ExpandConstant('{app}\lib\swt\swt.jar'), False);
    end;
  end;
end;

procedure CurUninstallStepChanged(CurUninstallStep: TUninstallStep);
var
  Items: TArrayOfString;
  NewItems: TArrayOfString;
  Path: String;
  NewPath: String;
  I: Integer;
begin
  if usPostUninstall = CurUninstallStep then
  begin
    RegQueryStringValue(HKEY_LOCAL_MACHINE,
      '{#PathKey}', 'Path', Path)
    Explode(Items, Path, ';');
    for I := Low(Items) to High(Items) do
    begin
      if Items[I] <> ExpandConstant('{app}\bin') then
      begin
        SetLength(NewItems, Length(NewItems) + 1);
        NewItems[Length(NewItems) - 1] := Items[I];
      end;
    end;
    NewPath := Join(NewItems, ';');
    RegWriteStringValue(HKEY_LOCAL_MACHINE,
      '{#PathKey}', 'Path', NewPath);
  end;
end;

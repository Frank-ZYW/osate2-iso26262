# OSATE2 ISO-26262 plugin

An [OSATE2](https://github.com/osate/osate2) plug-in that provides ISO-26262 support. It allows to run analytical method such as

- `FMEDA` (Failure Modes Effects and Diagnostic Coverage Analysis)
- `FMEA` (Failure Modes and Effects Analysis)
- `HARA` (Hazard Analysis and Risk Assessment)

from OSATE.

## Installation

1. Download & unzip the package from [latest release](https://github.com/Frank-ZYW/osate2-iso26262/releases).

2. Install plug-in from OSATE2 (***version >= 2.11.0***) via `Help` -> `Install New Software` -> `ADD` -> `Local` -> choose package path -> `Add` -> select package -> `Next` -> `Finish`.

   ![](doc/imgs/install.png) 

3. Trust all unsigned content, then restart OSATE.

## Usage

Choose an ***Instance File*** or ***Component Instance*** in the AADL Navigator, then select `Analyses` -> `ISO 26262` , and then the command to execute.

The output of the command (reports, etc.) is stored in the `./reports` folder in your project.

![](doc/imgs/usage.png) 

## Development

If you want to contribute to the project:

1. Follow [OSATE2 Developer Documentation](https://osate.org/setup-development.html) to set up a development environment uses the Eclipse Installer.
2. Use ***Git*** to clone the project, then import into Eclipse workspace.

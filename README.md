### Star Citizen - Data CaptureSession

This is a simple tool written in Java. - CIG's game Star Citizen is currently in alpha. The developers send some meta information
from the client to their servers to gather some statistics. This tool captures these packets so it does not interact with
any part of the game itself. This tool listens to all packets sent through the selected network interfaces and uses a
RegEx pattern to find the meta data packets from Star Citizen.

### Performance

The performance impact on the game is non-existing because, as mentioned above, we do not interact with the game in any form.
The typical performance footprint on the system itself is irrelevant as it almost does nothing but capturing and displaying.

### Do you collect any of the data?

No! We do not send the data to anyone but yourself. The data never leaves your computer and is only stored in memory until
you chose to store it on your computer's disk.

### Installation

You can download the latest version [here](https://github.com/starcitizendotguide/DataCapture/releases). It also requires
the installation of [Wireshark](https://www.wireshark.org/#download). Wireshark contains some of the necessary files that
this tool requires to capture the data without it the program wont work. 

### Features

 - Easy-to-use - Install the tool. Select the network interface. Done.
 - Multi-Session Management - The tool automatically detects if you start a new session (restart the game) and creates
 a new session, so you can keep all of your runs organized.
 - JSON Export - Do you wanna use the captured data anywhere else? No problem, you can output all sessions into files.

### Example

![Example Image](https://screenshotscdn.firefoxusercontent.com/images/7547b353-8fb0-405b-a0a4-4a21145a9c3d.png)


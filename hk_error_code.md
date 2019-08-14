| **错误类型**                        | 错误值 | 错误信息                                                     |
| ----------------------------------- | ------ | ------------------------------------------------------------ |
| **NET_DVR_NOERROR**                 | 0      | No error.                                                    |
| **NET_DVR_PASSWORD_ERROR**          | 1      | User name or password error.                                 |
| **NET_DVR_NOENOUGHPRI**             | 2      | Not authorized to do this operation.                         |
| **NET_DVR_NOINIT**                  | 3      | SDK is not initialized.                                      |
| **NET_DVR_CHANNEL_ERROR**           | 4      | Channel number error. There is no corresponding channel number on the  device. |
| **NET_DVR_OVER_MAXLINK**            | 5      | The number of connection with the device has exceeded the max  limit. |
| **NET_DVR_VERSIONNOMATCH**          | 6      | Version mismatch. SDK version is not matching with the device. |
| **NET_DVR_NETWORK_FAIL_CONNECT**    | 7      | Failed to connect to the device. The device is off-line, or connection  timeout caused by network. |
| **NET_DVR_NETWORK_SEND_ERROR**      | 8      | Failed to send data to the device.                           |
| **NET_DVR_NETWORK_RECV_ERROR**      | 9      | Failed to receive data from the device.                      |
| **NET_DVR_NETWORK_RECV_TIMEOUT**    | 10     | Timeout when receiving data from the device.                 |
| **NET_DVR_NETWORK_ERRORDATA**       | 11     | The data sent to the device is illegal, or the data received from the device  error. E.g. The  input data is not supported by the device for remote configuration. |
| **NET_DVR_ORDER_ERROR**             | 12     | API calling order error.                                     |
| **NET_DVR_OPERNOPERMIT**            | 13     | Not authorized for this operation.                           |
| **NET_DVR_COMMANDTIMEOUT**          | 14     | Executing command on the device is timeout.                  |
| **NET_DVR_ERRORSERIALPORT**         | 15     | Serial port number error. The assigned serial port does not exist on the  device. |
| **NET_DVR_ERRORALARMPORT**          | 16     | Alarm port number error.                                     |
| **NET_DVR_PARAMETER_ERROR**         | 17     | Parameter error. Input or output parameters in the SDK API is NULL,  or the value or format of the parameters does not match with the  requirement. |
| **NET_DVR_CHAN_EXCEPTION**          | 18     | Device channel is in exception status.                       |
| **NET_DVR_NODISK**                  | 19     | No hard disk on the device, and the operation of recording and hard disk  configuration will fail. |
| **NET_DVR_ERRORDISKNUM**            | 20     | Hard disk number error. The assigned hard disk number does not exist during  hard disk management. |
| **NET_DVR_DISK_FULL**               | 21     | Device hark disk is full.                                    |
| **NET_DVR_DISK_ERROR**              | 22     | Device hard disk error.                                      |
| **NET_DVR_NOSUPPORT**               | 23     | Device does not support this function.                       |
| **NET_DVR_BUSY**                    | 24     | Device is busy.                                              |
| **NET_DVR_MODIFY_FAIL**             | 25     | Failed to modify device parameters.                          |
| **NET_DVR_PASSWORD_FORMAT_ERROR**   | 26     | The inputting password format is not correct.                |
| **NET_DVR_DISK_FORMATING**          | 27     | Hard disk is formatting, and the operation cannot be done.   |
| **NET_DVR_DVRNORESOURCE**           | 28     | Not enough resource on the device.                           |
| **NET_DVR_DVROPRATEFAILED**         | 29     | Device operation failed.                                     |
| **NET_DVR_OPENHOSTSOUND_FAIL**      | 30     | Failed to collect local audio data or to open audio output during voice talk  / broadcasting. |
| **NET_DVR_DVRVOICEOPENED**          | 31     | Voice talk channel on the device has been occupied.          |
| **NET_DVR_TIMEINPUTERROR**          | 32     | Time input is not correct.                                   |
| **NET_DVR_NOSPECFILE**              | 33     | There is no selected file for playback.                      |
| **NET_DVR_CREATEFILE_ERROR**        | 34     | Failed to create a file, during local recording, saving picture, getting  configuration file or downloading record file. |
| **NET_DVR_FILEOPENFAIL**            | 35     | Failed to open a file.                                       |
| **NET_DVR_OPERNOTFINISH**           | 36     | The last operation has not been completed.                   |
| **NET_DVR_GETPLAYTIMEFAIL**         | 37     | Failed to get the current played time.                       |
| **NET_DVR_PLAYFAIL**                | 38     | Failed to start playback.                                    |
| **NET_DVR_FILEFORMAT_ERROR**        | 39     | The file format is not correct.                              |
| **NET_DVR_DIR_ERROR**               | 40     | File directory error.                                        |
| **NET_DVR_ALLOC_RESOURCE_ERROR**    | 41     | Resource allocation error.                                   |
| **NET_DVR_AUDIO_MODE_ERROR**        | 42     | Sound adapter mode error. Currently opened sound playing mode does not match  with the set mode. |
| **NET_DVR_NOENOUGH_BUF**            | 43     | Buffer is not enough.                                        |
| **NET_DVR_CREATESOCKET_ERROR**      | 44     | Create SOCKET error.                                         |
| **NET_DVR_SETSOCKET_ERROR**         | 45     | Set SOCKET error                                             |
| **NET_DVR_MAX_NUM**                 | 46     | The number of login or preview connections has exceeded the SDK  limitation. |
| **NET_DVR_USERNOTEXIST**            | 47     | User doest not exist. The user ID has been logged out or unavailable. |
| **NET_DVR_WRITEFLASHERROR**         | 48     | Writing FLASH error. Failed to write FLASH during device  upgrade. |
| **NET_DVR_UPGRADEFAIL**             | 49     | Failed to upgrade device. It is caused by network problem or the language  mismatch between the device and the upgrade file. |
| **NET_DVR_CARDHAVEINIT**            | 50     | The decode card has alreadly been initialed.                 |
| **NET_DVR_PLAYERFAILED**            | 51     | Failed to call API of player SDK.                            |
| **NET_DVR_MAX_USERNUM**             | 52     | The number of login user has reached the maximum limit.      |
| **NET_DVR_GETLOCALIPANDMACFAIL**    | 53     | Failed to get the IP address or physical address of local PC. |
| **NET_DVR_NOENCODEING**             | 54     | This channel hasn't started encoding.                        |
| **NET_DVR_IPMISMATCH**              | 55     | IP address not match                                         |
| **NET_DVR_MACMISMATCH**             | 56     | MAC address not match                                        |
| **NET_DVR_UPGRADELANGMISMATCH**     | 57     | The language of upgrading file does not match the language of the  device. |
| **NET_DVR_MAX_PLAYERPORT**          | 58     | The number of player ports has reached the maximum limit.    |
| **NET_DVR_NOSPACEBACKUP**           | 59     | No enough space to backup file in backup device.             |
| **NET_DVR_NODEVICEBACKUP**          | 60     | No backup device.                                            |
| **NET_DVR_PICTURE_BITS_ERROR**      | 61     | The color quality seeting of the picture does not match the requirement, and  it should be limited to 24. |
| **NET_DVR_PICTURE_DIMENSION_ERROR** | 62     | The dimension is over 128x256.                               |
| **NET_DVR_PICTURE_SIZ_ERROR**       | 63     | The size of picture is over 100K                             |
| **NET_DVR_LOADPLAYERSDKFAILED**     | 64     | Failed to load player SDK.                                   |
| **NET_DVR_LOADPLAYERSDKPROC_ERROR** | 65     | Can not find the function in player SDK.                     |
| **NET_DVR_LOADDSSDKFAILED**         | 66     | Failed to load the library file-"DsSdk".                     |
| **NET_DVR_LOADDSSDKPROC_ERROR**     | 67     | Can not find the API in "DsSdk".                             |
| **NET_DVR_DSSDK_ERROR**             | 68     | Failed to call the API in "DsSdk".                           |
| **NET_DVR_VOICEMONOPOLIZE**         | 69     | Sound adapter has been monopolized.                          |
| **NET_DVR_JOINMULTICASTFAILED**     | 70     | Failed to join to multicast group.                           |
| **NET_DVR_CREATEDIR_ERROR**         | 71     | Failed to create log file directory.                         |
| **NET_DVR_BINDSOCKET_ERROR**        | 72     | Failed to bind socket.                                       |
| **NET_DVR_SOCKETCLOSE_ERROR**       | 73     | Socket disconnected. It is caused by network disconnection or destination  unreachable. |
| **NET_DVR_USERID_ISUSING**          | 74     | The user ID is operating when logout.                        |
| **NET_DVR_SOCKETLISTEN_ERROR**      | 75     | Failed to listen                                             |
| **NET_DVR_PROGRAM_EXCEPTION**       | 76     | Sdk program exception                                        |
| **NET_DVR_WRITEFILE_FAILED**        | 77     | Failed to write file, during local recording, saving picture or downloading  record file. |
| **NET_DVR_FORMAT_READONLY**         | 78     | Failed to format read-only HD                                |
| **NET_DVR_WITHSAMEUSERNAME**        | 79     | This user name already exists in the user configuration structure. |
| **NET_DVR_DEVICETYPE_ERROR**        | 80     | Device type does not match when import configuration.        |
| **NET_DVR_LANGUAGE_ERROR**          | 81     | Language does not match when import configuration.           |
| **NET_DVR_PARAVERSION_ERROR**       | 82     | Software version does not match when import configuration.   |
| **NET_DVR_IPCHAN_NOTALIVE**         | 83     | IP channel is not on-line when previewing.                   |
| **NET_DVR_RTSP_SDK_ERROR**          | 84     | Load StreamTransClient.dll failed                            |
| **NET_DVR_CONVERT_SDK_ERROR**       | 85     | Load SystemTransform.dll failed                              |
| **NET_DVR_IPC_COUNT_OVERFLOW**      | 86     | over maximun ipc count                                       |
| **NET_DVR_MAX_ADD_NUM**             | 87     | add label or other operation reach the maximum number        |
| **NET_DVR_PARAMMODE_ERROR**         | 88     | Image intensifier, parameter mode error. This error may occur when client sets software or hardware  parameters. |
| **NET_DVR_CODESPITTER_OFFLINE**     | 89     | Code splitter is offline.                                    |
| **NET_DVR_BACKUP_COPYING**          | 90     | Device is backing up.                                        |
| **NET_DVR_CHAN_NOTSUPPORT**         | 91     | Channel not support                                          |
| **NET_DVR_CALLINEINVALID**          | 92     | The height line location is too concentrated, or the length line is not  inclined enough. |
| **NET_DVR_CALCANCELCONFLICT**       | 93     | Cancel calibration conflict, if the rule and overall actual size filter have  been set. |
| **NET_DVR_CALPOINTOUTRANGE**        | 94     | Calibration point exceeds the range.                         |
| **NET_DVR_FILTERRECTINVALID**       | 95     | The size filter does not meet the requirement.               |
| **NET_DVR_DDNS_DEVOFFLINE**         | 96     | Device has not registered to DDNS.                           |
| **NET_DVR_DDNS_INTER_ERROR**        | 97     | DDNS inner error.                                            |
| **NET_DVR_FUNCTION_NOT_SUPPORT_OS** | 98     | This function don't support this OS.                         |
| **NET_DVR_DEC_CHAN_REBIND**         | 99     | Decode channel can not bind with two display channel.        |
| **NET_DVR_INTERCOM_SDK_ERROR**      | 100    | Failed to load the audio intercom SDK from current directory. |
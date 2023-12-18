for %%i in (*.wav) do ffmpeg -i "%%i" -ac 1 -acodec pcm_u8 "converted\%%~ni.wav"

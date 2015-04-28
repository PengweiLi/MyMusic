# MyMusic

一. 设计思路

   本软件设计思路是现在流行的简单化设计，简洁大方。同时提高代码效率，以最小的代价实现用户常用功能。同时先实现基本功能，然后在添加复杂功能，优化UI。

二. 数据库设计

2.1 ContentProvider设计
    MyContentProvider继承了ContentProvider，几乎所有对数据库操作的功能都是由它进行封装的。在MyContentProvider中，我们创建了数据库，实现了对数据库的封装。MyContentProvider通过实现SQLiteOpenHelper的子类MainDatabaseHelper来完成对数据库的操作。
 
2.2 创建数据库
    本软件数据库用于用户账户信息的存储和查询。数据有4个字段，分别为_id，username，password，email。其中_id是用户id，是依次自动生成的；username是用户名，password是用户密码，email是用户邮箱。
         数据项                 类型
         _ID                    INTEGER
         UERNAME                TEXT
         PASSWORD               TEXT
         EMAIL                  TEXT
    创建数据库我们通过继承SQLiteOpenHelper类来实现，在onCreate的时候创建了数据库。在MyContentProvider类创建的时候生成MainDatabaseHelper的实例，之后我们就可以通过MianDatabaseHelper来获得可用于读或写的数据库了。这是通过mOpenHelper.getWritableDatabase()或mOpenHelper.getReadableDatabase()来实现的。

三.模块设计总述

    本软件执行音乐列表扫描，显示，以及音乐的播放。由于要在后台运行，本软件的主要逻辑主要在service中完成。同时在用户注册时采用数据库进行查找和存储，通过ContentProvider来对数据库操作。音乐更新与UI信息更新采用广播的方式。同时需要不同的界面处理不同的事务。以及一些公共数据的共享和处理。从整个activity栈分析，有2个分支，播放分支和注册登录分支。列表界面和播放界面同时绑定一个service，从service取数据来更新界面信息。程序的各个模块都考虑可扩展性。

3.1 音乐播放服务（Service）
    音乐播放服务作为一个远程服务，通过aidl与Activity通信，通过一个代理类实现aidl接口，然后通过管道进行数据传输。Service中在启动服务的时候新建一个MediaPlayer的实例，用于音乐的播放，同时返回给调用方一个内部类MusicBinder的实例。同时扫描数据库得到一个音乐列表。在Service中内部类MusicBinder完成主要的业务逻辑，同时会提供各种数据的获取方法。同时发广播通知界面更新UI。

3.2 列表显示界面（Activity）
    列表显示界面在程序启动的时候第一个显示的界面，主要加载音乐列表。主界面采用ListView来显示歌曲列表，列表每一个显示歌曲名，作者，时长信息。下面一部分显示当前播放歌曲，点击进入播放界面。同时长按ListItem弹出DialogFragment提示是否删除歌曲。删除文件先将文件删除，然后给MediaScanner发广播扫描文件，更新数据库。

3.3音乐播放界面（Activity）
    音乐播放界面显示播放信息，同时用SeekBar显示歌曲进度。控制下一首，上一首以及播放暂停。设置音乐播放顺序模式。同时可以滑动切换播放界面主题。在音乐播放中，会新建一个线程来不断地更新界面完成歌曲进度的显示。

3.4用户登录界面（Activity）
    这个模块提供用户登录的窗口，键入信息的Activity。它通过getContentResorver方法来获得MyContentProvider操作数据库，以检查登录信息是否正确。同时增加是否显示密码的选择。

3.5用户注册界面（Activity）
    提供用户信息的录入，同时判断用户名是否在数据库中存在。如果数据库不存在，会自动新建数据库，然后将新注册用户信息写入数据库。注册邮箱使用正则表达式进行合法性判断，并且使用确认密码保证密码输入正确。

四.模块结构

    模块结构是从主Activity（列表界面）开始，通过用户点击ListView或者button跳转到其他用于显示模块。同时显示模块绑定服务模块或者Provider。由于Activity绑定Service在onCreate中完成，所以列表界面会从2种状态从新进入。一种是回退到主界面，Activity已经销毁，重新加载UI需要在onServiceConnected中完成。如果进入下一个界面，Activity处于onPause状态，重新进入可以在onResume完成UI加载。
    列表界面和播放界面在启动时绑定播放服务，通过aidl与其通信，因为在界面加载UI要从播放服务中获取内容。登录界面在登录成功后会跳转到列表界面，并显示登录用户信息。在注册界面中，有用户名重复检测，信息完整检测，密码确认检测，邮箱正则表达式检测。注册成功会跳转到登录界面并自动填充登录信息。



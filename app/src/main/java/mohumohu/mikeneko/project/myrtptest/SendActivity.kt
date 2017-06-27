package mohumohu.mikeneko.project.myrtptest

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.net.rtp.AudioGroup
import android.net.rtp.AudioCodec
import android.net.rtp.AudioStream
import android.util.Log
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException




class SendActivity : AppCompatActivity() {
    private val TAG : String = "MainActivity"

    private var audioGroup: AudioGroup? = null
    private var audioStream: AudioStream? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send)
    }

    override fun onResume() {
        super.onResume()

        try {
            val localAddress = getLocalAddress()
            Log.d(TAG,"IP"+localAddress)
            val receiverAddress = InetAddress.getByName("192.168.31.49")

            audioStream = AudioStream(localAddress)
            audioStream?.codec = AudioCodec.PCMU
            audioStream?.mode = AudioStream.MODE_SEND_ONLY

            //2.AudioStreamに割り当てられたポート番号を受信側に伝えて、
            //  このポート番号を関連付けてもらいます。
            val senderPort = audioStream?.localPort

            //3.受信側から教えてもらったポート番号に音声ストリームを
            //  送信するように関連付けます。
            val receiverPort = 38922
            audioStream?.associate(receiverAddress, receiverPort)

            audioGroup = AudioGroup()
            audioGroup?.mode = AudioGroup.MODE_NORMAL
            Log.d(TAG,"ストリーム"+audioStream)
            audioStream?.join(audioGroup)
        } catch (e: Exception) {
            Log.e(TAG, "", e)
        }

    }

    override fun onPause() {
        super.onPause()

        if (audioGroup != null) {
            audioGroup!!.clear()
            audioGroup = null
        }
        if (audioStream != null) {
            audioStream!!.release()
            audioStream = null
        }
    }

    /** 最初に見つかったIPv4ローカルアドレスを返します。

     * @return
     * *
     * @throws SocketException
     */
        private fun getLocalAddress(): InetAddress? {
        val netifs = NetworkInterface.getNetworkInterfaces()
        while (netifs.hasMoreElements()) {
            val netif = netifs.nextElement()
            for (ifAddr in netif.getInterfaceAddresses()) {
                val a = ifAddr.getAddress()
                if (a != null && !a!!.isLoopbackAddress() && a is Inet4Address) {
                    return a
                }
            }
        }
        return null
    }
}

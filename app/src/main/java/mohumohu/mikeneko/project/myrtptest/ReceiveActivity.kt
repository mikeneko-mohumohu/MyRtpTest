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


class ReceiveActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private var audioGroup: AudioGroup? = null
    private var audioStream: AudioStream? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive)
    }

    override fun onResume() {
        super.onResume()

        try {
            val localAddress = getLocalAddress()
            Log.d(TAG,"IP"+localAddress)
            // 送信側Android端末のIPアドレス設定
            val senderAddress = InetAddress.getByName("192.168.31.57")

            audioStream = AudioStream(localAddress)
            audioStream?.codec = AudioCodec.PCMU
            audioStream?.mode = AudioStream.MODE_RECEIVE_ONLY

            //1.AudioStreamに割り当てられたポート番号を送信側に伝えて、
            //  このポート番号に送信してもらいます。
            val receiverPort = audioStream?.localPort
            Log.i(TAG, "#receiverPort=" + receiverPort)

            //4. 送信側から教えてもらったポート番号に関連付けます。
            //・・・ですが今回は双方向ではなく受信専用としているのでポート番号は適当でOKです。
            val senderPort = 54321
            audioStream?.associate(senderAddress, senderPort)

            audioGroup = AudioGroup()
            audioGroup?.mode = AudioGroup.MODE_MUTED
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

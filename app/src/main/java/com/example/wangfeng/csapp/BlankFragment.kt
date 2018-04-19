package com.example.wangfeng.csapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.wangfeng.csapp.dummy.DummyContent
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import kotlinx.android.synthetic.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [BlankFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [BlankFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class BlankFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        initView()
    }

    private fun initView () {

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_blank, container, false)
        Thread(Runnable {
            try {
                val cookieJar: ClearableCookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(MainAppliaction.context))
                val client = OkHttpClient.Builder().cookieJar(cookieJar).build()
                val request = Request.Builder()
                        .url("http://192.168.255.14:8000/user/info")
                        .build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {
                        println("something wrong")
                    }

                    @SuppressLint("SetTextI18n")
                    override fun onResponse(call: Call?, response: Response?) {
                        val res = response!!.body()!!.string()
//                            runOnUiThread({
//                                Toast.makeText(applicationContext, res, Toast.LENGTH_SHORT).show()
//                            })
                        Log.i("info", res)
                        if (response.code() == 200) {
                            val userNameView = view.findViewById<TextView>(R.id.userName)
                            val userScoreView = view.findViewById<TextView>(R.id.userScore)
                            val obj = JSONObject(res)
                            userNameView.text = "用户名：${obj.getString("user_name")}"
                            userScoreView.text = "用户积分：${obj.getString("score")}"
                        }
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).start()
        val logOutBtn = view.findViewById<Button>(R.id.logoutButton)
        logOutBtn.setOnClickListener({
            Thread(Runnable {
                try {
                    val cookieJar: ClearableCookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(MainAppliaction.context))
                    val client = OkHttpClient.Builder().cookieJar(cookieJar).build()
                    val request = Request.Builder()
                            .url("http://192.168.255.14:8000/user/logout")
                            .build()
                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call?, e: IOException?) {
                            println("something wrong")
                        }

                        @SuppressLint("SetTextI18n")
                        override fun onResponse(call: Call?, response: Response?) {
                            val res = response!!.body()!!.string()
//                            runOnUiThread({
//                                Toast.makeText(applicationContext, res, Toast.LENGTH_SHORT).show()
//                            })
                            Log.i("info", res)
                            if (response.code() == 200) {
                                val intent = Intent(MainAppliaction.context, Login::class.java)
                                startActivity(intent)
                            }
                        }
                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }).start()
        })
        return view
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BlankFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                BlankFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}

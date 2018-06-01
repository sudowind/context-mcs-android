package com.example.wangfeng.csapp

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.wangfeng.csapp.R.id.list

import com.example.wangfeng.csapp.dummy.QuestionContent
import com.example.wangfeng.csapp.dummy.QuestionContent.QuestionItem
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.*
import org.json.JSONArray
import java.io.IOException
import java.util.ArrayList


/**
 * A fragment representing a list of Items.
 *
 *
 * Activities containing this fragment MUST implement the [OnListFragmentInteractionListener]
 * interface.
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class QuestionFragment : Fragment() {
    // TODO: Customize parameters
    private var mColumnCount = 1
    private var mListener: OnListFragmentInteractionListener? = null
    private var data: MutableList<QuestionItem> = QuestionContent.ITEMS
    private var mAdapter : QuestionnaireViewAdapter? = null
    private var view : RecyclerView? = null

    var uiHandler = Handler(Handler.Callback { _ ->
        mAdapter = QuestionnaireViewAdapter(data, mListener)
        view?.adapter = mAdapter
        false
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mColumnCount = arguments.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        Log.i("on create view", data.count().toString())
        view = inflater!!.inflate(R.layout.fragment_item_list, container, false) as RecyclerView
        // Set the adapter
        if (view is RecyclerView) {
            val context = view?.context
            if (mColumnCount <= 1) {
                view?.layoutManager = LinearLayoutManager(context)
            } else {
                view?.layoutManager = GridLayoutManager(context, mColumnCount)
            }
            mAdapter = QuestionnaireViewAdapter(data, mListener)
            view?.adapter = mAdapter
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        loadQuestionnaires()
    }

    private fun loadQuestionnaires() {
        Thread(Runnable {
            try {
                val cookieJar: ClearableCookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(MainAppliaction.context))
                val client = OkHttpClient.Builder().cookieJar(cookieJar).build()
                val request = Request.Builder()
                        .url("http://www.sudowind.com:8000/user/questionnaire_list")
                        .build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {
                        println("something wrong")
                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        val res = response!!.body()!!.string()
//                            runOnUiThread({
//                                Toast.makeText(applicationContext, res, Toast.LENGTH_SHORT).show()
//                            })
                        if (response.code() == 200) {
                            val obj = JSONArray(res)
                            Log.i("load content", obj.length().toString())
                            data = ArrayList<QuestionItem>()
                            for (i in 0..(obj.length() - 1))
                            {
                                val status : Int = obj.getJSONObject(i).get("status") as Int
                                var statusString : String = ""
                                when (status) {
                                    0 -> { statusString = "待完成"}
                                    1 -> { statusString = "已提交"}
                                    2 -> { statusString = "已过期"}
                                }
                                data.add(QuestionItem(
                                        (i + 1).toString(),
                                        obj.getJSONObject(i).getJSONObject("qn").getString("name"),
                                        "截止时间：" + Utils().getTimeStr((obj.getJSONObject(i).get("end_timestamp")).toString().toLong()),
                                        obj.getJSONObject(i).get("uq_id").toString(),
                                        statusString,
                                        "questionnaire"
                                ))
                            }
                            uiHandler.sendEmptyMessage(0)
                        }
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).start()
    }

    override fun onResume() {
        super.onResume()
        Log.i("on resume", "fragment resume")
        loadQuestionnaires()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Log.i("on attach", "attached")
        if (context is OnListFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onListFragmentInteraction(item: QuestionItem)
    }

    companion object {

        // TODO: Customize parameter argument names
        private val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        fun newInstance(columnCount: Int): QuestionFragment {
            val fragment = QuestionFragment()
            val args = Bundle()
            args.putInt(ARG_COLUMN_COUNT, columnCount)
            fragment.arguments = args
            return fragment
        }
    }
}

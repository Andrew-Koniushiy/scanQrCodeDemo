package com.github.akoniushiy.scanQrCodeDemo.fragments

import android.app.ActivityOptions
import android.app.SearchManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.github.akoniushiy.scanQrCodeDemo.R
import java.net.URLEncoder


class ResultFragment : Fragment() {
    private val args: ResultFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val scanResult = args.scanResult
        view.findViewById<TextView>(R.id.scanResult).text = scanResult
        view.findViewById<Button>(R.id.viewButton).setOnClickListener {
            val uri = Uri.parse(scanResult)
            var intent = Intent(Intent.ACTION_VIEW, uri)
            if(uri.scheme.isNullOrEmpty()){
                intent = Intent(Intent.ACTION_WEB_SEARCH)
                intent.putExtra(SearchManager.QUERY, scanResult)
            }
            startActivity(
                intent,
                ActivityOptions.makeCustomAnimation(
                    requireContext(),
                    R.anim.navigation_enter_anim,
                    R.anim.navigation_exit_anim
                ).toBundle()
            )
        }
    }

}
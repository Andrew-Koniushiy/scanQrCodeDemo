package com.github.akoniushiy.scanQrCodeDemo.fragments

import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.github.akoniushiy.scanQrCodeDemo.R

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.splashButton).setOnClickListener {
            findNavController().navigate(R.id.action_splashFragment_to_mainActivity)
        }
        view.findViewById<Button>(R.id.splashGoToSrcButton).setOnClickListener {
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(resources.getString(R.string.github_link))),
                ActivityOptions.makeCustomAnimation(
                    requireContext(),
                    R.anim.navigation_enter_anim,
                    R.anim.navigation_exit_anim
                ).toBundle()
            )
        }
    }
}
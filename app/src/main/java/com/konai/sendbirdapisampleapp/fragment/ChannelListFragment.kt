package com.konai.sendbirdapisampleapp.fragment

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.konai.sendbirdapisampleapp.R
import com.konai.sendbirdapisampleapp.adapter.ChannelListAdapter
import com.konai.sendbirdapisampleapp.databinding.FragmentChannelBinding
import com.konai.sendbirdapisampleapp.model.ChannelListModel
import com.konai.sendbirdapisampleapp.util.Constants.USER_ID
import com.konai.sendbirdapisampleapp.util.Extension.convertLongToTime
import com.konai.sendbirdapisampleapp.util.Extension.toast
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.channel.query.GroupChannelListQueryOrder
import com.sendbird.android.channel.query.MyMemberStateFilter
import com.sendbird.android.params.GroupChannelCreateParams
import com.sendbird.android.params.GroupChannelListQueryParams

class ChannelListFragment : BaseFragment<FragmentChannelBinding>(R.layout.fragment_channel) {
    private var _channelList: MutableList<ChannelListModel> = mutableListOf()


    override fun initView() {
        super.initView()


        initRecyclerView()


        //TODO clean code
        binding.createChannelLayoutButton.setOnClickListener {
            onCreateChannelButtonClicked()
        }
    }



    private fun initRecyclerView() {
        initChannelList()
        Log.d("1111", "channel List2 : $_channelList")

        val adapter = ChannelListAdapter(requireContext())
        adapter.channelList = _channelList
        binding.chatListRecyclerView.adapter = adapter
        binding.chatListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        Log.d("1111", "channel List4 : $_channelList")
    }



    private fun initChannelList() {
        val query = GroupChannel.createMyGroupChannelListQuery(
            GroupChannelListQueryParams().apply {
                includeEmpty = true
                myMemberStateFilter = MyMemberStateFilter.JOINED
                order = GroupChannelListQueryOrder.LATEST_LAST_MESSAGE
            }
        )

        query.next { channels, e ->
            if (e != null) {
                requireContext().toast("$e")
                return@next
            }

            _channelList.clear() //to make the list empty

            if (channels!!.isEmpty()){
                binding.emptyChannelCoverTextView.visibility = View.VISIBLE
                return@next
            }

            for (i in channels.indices) {
                _channelList.add(
                    ChannelListModel(
                        name = channels[i].name,
                        url = channels[i].url,
                        lastMessage = channels[i].lastMessage?.message,
                        lastMessageTime = (channels[i].lastMessage?.createdAt)?.convertLongToTime()
                    )
                )
            }
            binding.chatListRecyclerView.adapter?.notifyDataSetChanged()
        }
    }

    //TODO dataBinding onClicked
    private fun onCreateChannelButtonClicked() {
        val invitedUserId = binding.userIdInputEditText.text.toString()
        val users: List<String> = listOf(USER_ID, invitedUserId)
        val params = GroupChannelCreateParams().apply {
            userIds = users
            isDistinct = true
            name = "$USER_ID, $invitedUserId"
            isSuper = false
        }

        GroupChannel.createChannel(params) { channel, e ->
            if (e != null) {
                requireContext().toast("$e")
            }
            if (channel != null) {
                Toast.makeText(requireContext(), "채팅방 생성", Toast.LENGTH_SHORT).show()
                //TODO refresh UI
            }
        }
        binding.userIdInputEditText.text = null
    }
}
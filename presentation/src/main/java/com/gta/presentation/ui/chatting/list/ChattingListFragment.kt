package com.gta.presentation.ui.chatting.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gta.presentation.R
import com.gta.presentation.databinding.FragmentChattingListBinding
import com.gta.presentation.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory
import javax.inject.Inject

@AndroidEntryPoint
class ChattingListFragment : BaseFragment<FragmentChattingListBinding>(
    R.layout.fragment_chatting_list
) {
    // 채팅 작업이 필요할 땐 얘를 부르면 됩니다
    @Inject
    lateinit var chatClient: ChatClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val channelListViewModel: ChannelListViewModel by viewModels {
            ChannelListViewModelFactory()
        }
        channelListViewModel.bindView(binding.clChattingList, viewLifecycleOwner)
        binding.clChattingList.setChannelItemClickListener { channel ->
            findNavController().navigate(
                ChattingListFragmentDirections.actionChattingListFragmentToChattingFragment(channel.cid)
            )
        }
        binding.clChattingList.setChannelLongClickListener { channel ->
            chatClient.deleteChannel(channel.type, channel.id).enqueue()
            true
        }
    }
}
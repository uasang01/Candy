package com.example.candy.challenge.pagerFragment

import android.opengl.Visibility
import android.os.Bundle
import android.provider.SyncStateContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ViewSwitcher
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.candy.adapter.HomeCategoryRecyclerAdapter
import com.example.candy.adapter.HorizontalItemDecorator
import com.example.candy.adapter.VerticalItemDecorator
import com.example.candy.challenge.adapter.PossibleChallengeRecyclerAdapter
import com.example.candy.challenge.adapter.categoryRecyclerAdapter.ChallengeCategoryRecyclerAdapter
import com.example.candy.challenge.viewmodel.PossibleChallengeViewModel
import com.example.candy.databinding.FragmentLikeChallengeBinding
import com.example.candy.databinding.FragmentPossibleChallengeBinding
import com.example.candy.model.data.Challenge
import com.example.candy.model.injection.Injection
import com.example.candy.utils.Constants
import com.example.candy.utils.CurrentUser


class PossibleFragment: Fragment() {

    private var possibleChallengeBinding: FragmentPossibleChallengeBinding? = null

    private val categoryList = arrayListOf<String>()  // 카테고리
    //private lateinit var possibleChallengeList : List<Challenge>
    private lateinit var viewModel: PossibleChallengeViewModel

    private var page = 1 // 리스트 10개가 1page
    private var noMoreData = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? {
        val binding = FragmentPossibleChallengeBinding.inflate(inflater, container, false)
        possibleChallengeBinding = binding

        return possibleChallengeBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("fragment check","PossibleFragment onViewCreated")
        Log.d("token check", CurrentUser.userToken!!)

        // 카테고리 recycler
        categoryList.add("ALL")
        categoryList.add("영어")
        categoryList.add("수학")
        categoryList.add("국어")
        categoryList.add("과학")
        categoryList.add("한국사")

        possibleChallengeBinding!!.recyclerPossibleChallengeCategory.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        possibleChallengeBinding!!.recyclerPossibleChallengeCategory.adapter = ChallengeCategoryRecyclerAdapter(categoryList)
        possibleChallengeBinding!!.recyclerPossibleChallengeCategory.addItemDecoration(HorizontalItemDecorator(20))



        possibleChallengeBinding!!.recyclerPossibleChallenge.layoutManager = LinearLayoutManager(context)
        possibleChallengeBinding!!.recyclerPossibleChallenge.adapter = PossibleChallengeRecyclerAdapter(ArrayList<Challenge>()) //
        possibleChallengeBinding!!.recyclerPossibleChallenge.addItemDecoration(VerticalItemDecorator(10))


        viewModel = ViewModelProvider(viewModelStore, object: ViewModelProvider.Factory{
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return PossibleChallengeViewModel(
                    Injection.provideRepoRepository(context!!)
                ) as T
            }
        }).get(PossibleChallengeViewModel::class.java)

        // 도전 가능 챌린지 리스트 observe
        viewModel.possibleChallengeLiveData.observe(viewLifecycleOwner,{
            (possibleChallengeBinding!!.recyclerPossibleChallenge.adapter as PossibleChallengeRecyclerAdapter).updateList(it)

            (possibleChallengeBinding!!.recyclerPossibleChallenge.adapter as PossibleChallengeRecyclerAdapter).
                    notifyItemRangeChanged((page-1) * 10, it.size)

            // 마지막 목록이면 더 이상 데이터가 없으므로 progressbar 제거해주기!!
            if(it.size == 0){
                (possibleChallengeBinding!!.recyclerPossibleChallenge.adapter as PossibleChallengeRecyclerAdapter).deleteLoading()
                noMoreData = true
            }
        })

        // progressbar observe
        viewModel.progressVisible.observe(viewLifecycleOwner, {
            if(it){
                possibleChallengeBinding!!.progressbar.visibility = View.VISIBLE
            }
            else{
                possibleChallengeBinding!!.progressbar.visibility = View.GONE
            }
        })

        // 리사클러뷰 무한스크롤 구현
        possibleChallengeBinding!!.recyclerPossibleChallenge.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val lastVisibleItemViewPosition =
                        (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
                val totalItemViewCount = recyclerView.adapter!!.itemCount-1

                // 스크롤 마지막에 도달 시
                if(lastVisibleItemViewPosition == totalItemViewCount){
                    (possibleChallengeBinding!!.recyclerPossibleChallenge.adapter as PossibleChallengeRecyclerAdapter).deleteLoading()

                    var lastChallengeId = (possibleChallengeBinding!!.recyclerPossibleChallenge.adapter as PossibleChallengeRecyclerAdapter)
                                .getLastChallengeId(totalItemViewCount - 1)

                    // 페이지 증가
                    page++


                    viewModel.getAllPossibleChallengeList(lastChallengeId, 10, false)



                }

            }
        })

    }

    override fun onResume() {
        super.onResume()
        Log.d("fragment check","PossibleFragment onResume")

        (possibleChallengeBinding!!.recyclerPossibleChallenge.adapter as PossibleChallengeRecyclerAdapter).dataSetClear()
        viewModel.getAllPossibleChallengeList(100000000, 10, true)
    }

    override fun onDestroyView() {
        possibleChallengeBinding = null
        super.onDestroyView()
    }
}
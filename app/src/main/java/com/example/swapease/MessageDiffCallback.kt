package com.example.swapease

import androidx.recyclerview.widget.DiffUtil
import com.example.swapease.data.models.Message
import javax.annotation.Nullable

class MessageDiffCallback(oldEmployeeList: List<Message>, newEmployeeList: List<Message>) :
        DiffUtil.Callback() {
        private val mOldEmployeeList: List<Message>
        private val mNewEmployeeList: List<Message>

        init {
            mOldEmployeeList = oldEmployeeList
            mNewEmployeeList = newEmployeeList
        }

        override fun getOldListSize(): Int {
            return mOldEmployeeList.size
        }

        override fun getNewListSize(): Int {
            return mNewEmployeeList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return mOldEmployeeList[oldItemPosition].messageId === mNewEmployeeList[newItemPosition].messageId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldEmployee: Message = mOldEmployeeList[oldItemPosition]
            val newEmployee: Message = mNewEmployeeList[newItemPosition]
            return oldEmployee.text == newEmployee.text
        }

        @Nullable
        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            // Implement method if you're going to use ItemAnimator
            return super.getChangePayload(oldItemPosition, newItemPosition)
        }
    }
package com.zaihuishou.databinding.expandablerecycleradapter.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.zaihuishou.databinding.expandablerecycleradapter.observable.BaseExpandableObservable;
import com.zaihuishou.databinding.expandablerecycleradapter.util.AdapterItemUtil;
import com.zaihuishou.databinding.expandablerecycleradapter.viewholder.BindingViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * * creater: zaihuishou
 * create time: 16-8-2
 * author email:tanzhiqiang.cathy@gmail.com
 * desc: the data binding expandable recyclerview adapter
 */

public abstract class BaseExpandableAdapter extends RecyclerView.Adapter<BindingViewHolder>
        implements BaseExpandableObservable.ItemListExpandCollapseListener {

    protected List<Object> mDataList;

    private int mItemType;

    private AdapterItemUtil mUtil = new AdapterItemUtil();

    private BaseExpandableAdapter.ExpandCollapseListener mExpandCollapseListener;

    private LayoutInflater mLayoutInflater;

    private Context mContext;
    private ArrayList<RecyclerView> mRecyclerViews;

    public BaseExpandableAdapter(Context context, List<Object> dataList) {
        this.mContext = context;
        mDataList = dataList;
        mRecyclerViews = new ArrayList<>();
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerViews.add(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerViews.remove(recyclerView);
    }

    /**
     * update all item
     *
     * @param newDataList thw new data list
     */
    public void updateData(@NonNull List<Object> newDataList) {
        if (newDataList != null && !newDataList.isEmpty()) {
            mDataList = newDataList;
            notifyDataSetChanged();
        }
    }

    /**
     * update an exit item
     *
     * @param position
     * @param o
     */
    public void updateItem(int position, Object o) {
        if (!isDataListEmpty() && position >= 0 && position < mDataList.size()) {
            mDataList.set(position, o);
            notifyItemChanged(position);
        }
    }

    /**
     * inserted an item to position
     *
     * @param position the item will be add to position
     * @param o        item object
     */

    public void addItem(int position, Object o) {
        if (!isDataListEmpty() && position >= 0 && position <= mDataList.size()) {
            mDataList.add(position, o);
            notifyItemInserted(position);
        }
    }

    /**
     * delete an item by position
     *
     * @param position
     */
    public void deleteItem(int position) {
        if (!isDataListEmpty() && position >= 0 && position < mDataList.size()) {
            mDataList.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * @return {@link #mDataList} is empty
     */
    private boolean isDataListEmpty() {
        return (mDataList == null || mDataList.isEmpty()) ? true : false;
    }

    @Override
    public void onItemListCollapsed(BaseExpandableObservable baseExpandableObservable) {
        int indexOf = mDataList.indexOf(baseExpandableObservable);
        collapseListItem(indexOf, baseExpandableObservable, true);
    }

    @Override
    public void onItemListExpanded(BaseExpandableObservable baseExpandableObservable) {
        int indexOf = mDataList.indexOf(baseExpandableObservable);
        expandItem(indexOf, baseExpandableObservable, true);
    }

    /**
     * expand item
     *
     * @param parentIndex                       will be expand item index
     * @param baseExpandableObservable          the item Observable
     * @param expansionTriggeredByListItemClick
     */
    public void expandItem(int parentIndex, BaseExpandableObservable baseExpandableObservable, boolean expansionTriggeredByListItemClick) {
        if (parentIndex >= 0 && parentIndex < mDataList.size() && !baseExpandableObservable.isExpand.get()) {

            ObservableArrayList<Object> childList = baseExpandableObservable.getChildList();
            final int childSize = childList.size();
            for (int i = 0; i < childSize; i++) {
                Object o = childList.get(i);
                int newIndex = parentIndex + i + 1;
                mDataList.add(newIndex, o);
            }
            notifyItemRangeInserted(parentIndex + 1, childSize);
            int positionStart = parentIndex + childSize;
            if (parentIndex != mDataList.size() - 1)
                notifyItemRangeChanged(positionStart, mDataList.size() - positionStart);
            baseExpandableObservable.setIsExpand(true);
            if (!mRecyclerViews.isEmpty()) {
                BindingViewHolder viewHolderForAdapterPosition = (BindingViewHolder) mRecyclerViews.get(0).findViewHolderForAdapterPosition(parentIndex);
                baseExpandableObservable.onExpansionToggled(viewHolderForAdapterPosition, parentIndex, true);
            }
            if (expansionTriggeredByListItemClick && mExpandCollapseListener != null) {
                mExpandCollapseListener.onListItemExpanded(parentIndex);
            }
        }
    }


    /**
     * @param baseExpandableObservable         {@link BaseExpandableObservable}
     * @param parentIndex                      item index
     * @param collapseTriggeredByListItemClick
     */

    private void collapseListItem(int parentIndex, BaseExpandableObservable baseExpandableObservable, boolean collapseTriggeredByListItemClick) {
        if (baseExpandableObservable.isExpand.get()) {
            ObservableArrayList<Object> childItemList = baseExpandableObservable.getChildList();
            if (childItemList != null && !childItemList.isEmpty()) {
                int childListItemCount = childItemList.size();
                for (int i = childListItemCount - 1; i >= 0; i--) {
                    int index = parentIndex + i + 1;
                    Object o = mDataList.get(index);
                    if (o instanceof BaseExpandableObservable) {
                        BaseExpandableObservable parentListItem;
                        try {
                            parentListItem = (BaseExpandableObservable) o;
                            collapseListItem(index, parentListItem, false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    mDataList.remove(index);
                }

                notifyItemRangeRemoved(parentIndex + 1, childListItemCount);
                baseExpandableObservable.setIsExpand(false);
                if (!mRecyclerViews.isEmpty()) {
                    BindingViewHolder viewHolderForAdapterPosition = (BindingViewHolder) mRecyclerViews.get(0).findViewHolderForAdapterPosition(parentIndex);
                    baseExpandableObservable.onExpansionToggled(viewHolderForAdapterPosition, parentIndex, false);
                }
                notifyItemRangeChanged(parentIndex + 1, mDataList.size() - parentIndex - 1);
            }

            if (collapseTriggeredByListItemClick && mExpandCollapseListener != null) {
                int expandedCountBeforePosition = getExpandedItemCount(parentIndex);
                mExpandCollapseListener.onListItemCollapsed(parentIndex - expandedCountBeforePosition);
            }
        }
    }

    /**
     * Collapses all parents in the list.
     */
    public void collapseAllParents() {
        if (mDataList != null && !mDataList.isEmpty()) {
            ArrayList<Object> expandableListItems = getParents(true);
            if (expandableListItems != null && !expandableListItems.isEmpty()) {
                final int expandedItemSize = expandableListItems.size();
                if (expandedItemSize > 0) {
                    for (int i = 0; i < expandedItemSize; i++) {
                        Object o = expandableListItems.get(i);
                        int indexOf = mDataList.indexOf(o);
                        if (indexOf >= 0)
                            collapseListItem(indexOf, (BaseExpandableObservable) o, false);
                    }
                }
            }
        }
    }

    /**
     * @param isExpanded
     * @return return all expanded items
     */
    @NonNull
    private ArrayList<Object> getParents(boolean isExpanded) {
        final int size = mDataList.size();
        ArrayList<Object> expandableListItems = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Object o = mDataList.get(i);
            if (o instanceof BaseExpandableObservable) {
                BaseExpandableObservable expandableListItem = (BaseExpandableObservable) o;
                if (isExpanded) {
                    if (expandableListItem.isExpand.get())
                        expandableListItems.add(o);
                } else {
                    if (!expandableListItem.isExpand.get())
                        expandableListItems.add(o);
                }
            }
        }
        return expandableListItems;
    }

    /**
     * Gets the number of expanded child list items before the specified position.
     *
     * @param position The index before which to return the number of expanded
     *                 child list items
     * @return The number of expanded child list items before the specified position
     */
    private int getExpandedItemCount(int position) {
        if (position == 0) {
            return 0;
        }

        int expandedCount = 0;
        for (int i = 0; i < position; i++) {
            Object listItem = getListItem(i);
            if (!(listItem instanceof BaseExpandableObservable)) {
                expandedCount++;
            }
        }
        return expandedCount;
    }

    /**
     * Gets the list item held at the specified adapter position.
     *
     * @param position The index of the list item to return
     * @return The list item at the specified position
     */
    protected Object getListItem(int position) {
        boolean indexInRange = position >= 0 && position < mDataList.size();
        if (indexInRange) {
            return mDataList.get(position);
        } else {
            return null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        mItemType = getItemViewType(mDataList.get(position));
        return mUtil.getIntType(mItemType);
    }

    public int getItemViewType(Object t) {
        return -1;
    }

    /**
     * @param type item type
     * @return item layout
     */
    @LayoutRes
    @NonNull
    public abstract int getItemLayout(int type);

    @Override
    public BindingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BindingViewHolder(DataBindingUtil.inflate(mLayoutInflater, getItemLayout(mItemType), parent, false));
    }

    @Override
    public void onBindViewHolder(BindingViewHolder holder, int position) {
        Object o = mDataList.get(position);
        ViewDataBinding binding = holder.getBinding();
        binding.setVariable(getVariable(o, position), o);
        binding.executePendingBindings();
        if (o instanceof BaseExpandableObservable) {
            BaseExpandableObservable baseExpandableObservable = (BaseExpandableObservable) o;
            ObservableArrayList<Object> childList = baseExpandableObservable.getChildList();
            if (childList != null && !childList.isEmpty())
                baseExpandableObservable.setItemListExpandCollapseListener(this);
        }
    }

    /**
     * @param o     item object
     * @param index item index
     * @return item BR type
     */
    public abstract int getVariable(Object o, int index);

    /**
     * @return data list
     */
    public List<?> getDataList() {
        return mDataList;
    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    public interface ExpandCollapseListener {

        /**
         * Called when a list item is expanded.
         *
         * @param position The index of the item in the list being expanded
         */
        void onListItemExpanded(int position);

        /**
         * Called when a list item is collapsed.
         *
         * @param position The index of the item in the list being collapsed
         */
        void onListItemCollapsed(int position);
    }

    public void setExpandCollapseListener(ExpandCollapseListener expandCollapseListener) {
        mExpandCollapseListener = expandCollapseListener;
    }
}

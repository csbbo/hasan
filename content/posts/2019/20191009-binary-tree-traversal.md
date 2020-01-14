---
title: "二叉树遍历"
date: 2019-10-09T22:26:30+08:00
toc: true
tags: ["algorithm"]
categories: ["算法"]
---

二叉树的前中后序遍历，使用递归算法实现最为简单，他们的差别仅在于输出时机的不同。

<!--more-->

```cpp
vector<int> ret;
vector<int> preorderTraversal(TreeNode* root) {
    backtraking(root);
    return ret;
}
void backtraking(TreeNode* root){
    if(root == NULL)
        return ;
    preorderTraversal(root->left);
    ret.push_back(root->val);
    preorderTraversal(root->right);
}
```

### 非递归前序遍历[LeetCode 144](https://leetcode.com/problems/binary-tree-preorder-traversal/)

二叉树的非递归遍历，主要的思想是使用栈（Stack）来进行存储操作，记录经过的节点。

```cpp
vector<int> preorderTraversal(TreeNode* root) {
    vector<int> ret;
    if(root==NULL)
        return ret;
    stack<TreeNode *> s;
    while(root || !s.empty()){
        if(root){
            s.push(root);
            ret.push_back(root->val);
            root = root->left;
        }else{
            root = s.top();
            root = root->right;
            s.pop();
        }
    }
    return ret;
}
```

### 非递归中序遍历[LeetCode 94](https://leetcode.com/problems/binary-tree-inorder-traversal/)

非递归中序遍历跟非递归前序遍历一样，只需要改一下输出的位置

```cpp
vector<int> inorderTraversal(TreeNode* root) {
    vector<int> ret;
    if(root==NULL)
        return ret;
    stack<TreeNode *> s;
    while(root || !s.empty()){
        if(root){
            s.push(root);
            root = root->left;
        }else{
            root = s.top();
            ret.push_back(root->val);
            root = root->right;
            s.pop();
        }
    }
    return ret;
}
```

### 非递归后序遍历[LeetCode 145](https://leetcode.com/problems/binary-tree-postorder-traversal/)

非递归遍历中，后序遍历相对更难实现，因为需要在遍历完左右子节点之后，再遍历根节点，因此不能直接将根节点出栈。这里使用一个 last 指针记录上次出栈的节点，当且仅当节点的右孩子为空（top->right == NULL），或者右孩子已经出栈（top->right == last），才将本节点出栈。

因此后序遍历在前序遍历或中序遍历的基础上多加一个top指针指向当前要出栈节点和last指针指向上次出栈节点，

然后判断右孩子为空或右孩子出栈。

```cpp
vector<int> postorderTraversal(TreeNode* root) {
    vector<int> ret;
    if(!root)
        return ret;
    TreeNode *top,*last = NULL;
    stack<TreeNode *> s;
    while(root || !s.empty()){
        if(root){
            s.push(root);
            root = root->left;
        }else{
            top = s.top();
            if(top->right==NULL || top->right==last){
                s.pop();
                ret.push_back(top->val);
                last = top;
            }else{
                root = top->right;
            }
        }
    }
    return ret;
}
```

### 层序遍历 [LeetCode 102](https://leetcode.com/problems/binary-tree-level-order-traversal/)

#### 深度优先（DFS）

```cpp
void traversal(TreeNode *root, int level, vector<vector<int>> &result) {
    if (!root)
        return;
    // 保证每一层只有一个vector
    if (level > result.size()) {
        result.push_back(vector<int>());
    }
    result[level-1].push_back(root->val);
    traversal(root->left, level+1, result);
    traversal(root->right, level+1, result);
}

vector<vector<int> > levelOrder(TreeNode *root) {
    vector<vector<int>> result;
    traversal(root, 1, result);
    return result;
}
```

#### 广度优先（BFS）

```cpp
vector<vector<int>> levelOrder(TreeNode* root) {
    std:queue<TreeNode *> q;
    TreeNode *p;

    vector<vector<int>> result;
    if (root == NULL) return result;

    q.push(root);

    while (!q.empty()) {
        int size = q.size();
        vector<int> levelResult;

        for (int i = 0; i < size; i++) {
            p = q.front();
            q.pop();

            levelResult.push_back(p->val);

            if (p->left) {
                q.push(p->left);
            }
            if (p->right) {
                q.push(p->right);
            }
        }

        result.push_back(levelResult);
    }

    return result;
}
```
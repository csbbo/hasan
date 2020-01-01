/*
创建一颗二叉树，打印二叉树，打印动态二维数组vector
*/

#include<iostream>
#include<cstdlib>
#include<queue>
#include<vector>
using namespace std;

struct TreeNode {
	int val;
	struct TreeNode *left;
	struct TreeNode *right;
	TreeNode(int x) :val(x), left(NULL), right(NULL){}
};

TreeNode* CreateBinaryTree(int n){
    if(n<=0)
        return NULL;
    TreeNode* node = new TreeNode(rand()%10);
    node->left = CreateBinaryTree(n-1);
    node->right = CreateBinaryTree(n-1);
}

void PrintBinaryTree(TreeNode* root){
    if(root==NULL)
        return;
    queue<TreeNode*> q;
    q.push(root);
    while(!q.empty()){
        int len = q.size();
        for(int i=0;i<len;i++){
            TreeNode* node = q.front();
            q.pop();
            cout<<node->val<<" ";
            if(node->left)
                q.push(node->left);
            if(node->right)
                q.push(node->right);
        }
        cout<<endl;
    }
}

void PrintVector2(vector<vector<int> > &list){
    for(auto const &elem : list){
        for(auto const &e : elem){
            cout<<e<<" ";
        }
        cout<<endl;
    }
}

int main(){
    return 0;
}

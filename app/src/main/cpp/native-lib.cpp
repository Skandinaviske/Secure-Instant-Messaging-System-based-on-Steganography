//
// Created by pc on 2017/12/18.
//

#include <openssl/dh.h>
#include <memory.h>
#include <iostream>
#include <jni.h>
#include <openssl/ossl_typ.h>

using namespace std;

dh_st *D;
extern"C"{
jobject
Java_com_tencent_qcloud_timchat_ui_ChatActivity_initpg(
JNIEnv *env,
jobject thiz/* this */)
 {
    BIGNUM *P,*G;
 	D = DH_new();
 	int ret, size, i;
 	ret = DH_generate_parameters_ex(D, 64, DH_GENERATOR_2, NULL);
 	P = D->p;
    G = D->g;

     jclass list_cls = env->FindClass("Ljava/util/ArrayList;");
    jmethodID list_costruct = env->GetMethodID(list_cls , "<init>","()V"); //获得得构造函数Id
    jobject list_obj = env->NewObject(list_cls , list_costruct); //创建一个Arraylist集合对象
    jmethodID list_add  = env->GetMethodID(list_cls,"add","(Ljava/lang/Object;)Z");
    jclass bignums = env->FindClass("app/java/com/tencent/qcloud/timchat/ui/ChatActivity/BIGNUM;");
    jmethodID nummethod = env->GetMethodID(bignums , "<init>", "()V");
    jfieldID top = env->GetFieldID(bignums,"top","I");
    jfieldID dmax = env->GetFieldID(bignums,"dmax","I");
    jfieldID neg = env->GetFieldID(bignums,"neg","I");
    jfieldID flags = env->GetFieldID(bignums,"flags","I");
    jfieldID longnum = env->GetFieldID(bignums,"longnum","L");


    jobject userp = env->NewObject(bignums,nummethod);
     env->SetIntField(bignums,top,P->top);
    env->SetIntField(bignums,dmax,P->dmax);
    env->SetIntField(bignums,neg,P->neg);
    env->SetIntField(bignums,flags,P->flags);
    env->SetIntField(bignums,longnum,*(P->d));
    env->CallBooleanMethod(list_obj , list_add , userp);
    jobject userg = env->NewObject(bignums,nummethod);
    env->SetIntField(bignums,top,G->top);
    env->SetIntField(bignums,dmax,G->dmax);
    env->SetIntField(bignums,neg,G->neg);
    env->SetIntField(bignums,flags,G->flags);
    env->SetIntField(bignums,longnum,*(G->d));
    env->CallBooleanMethod(list_obj , list_add , userg);
 	return list_obj;
}

int receiver(){
	int ret,len1,len2;
	DH *d2;
	d2 = DH_new();
	d2->p = BN_dup(D->p);
	d2->g = BN_dup(D->g);
	ret = DH_generate_key(d2);
	char sharekey1[128], sharekey2[128];

	/* 计算共享密钥 */
	len1 = DH_compute_key((unsigned char*)sharekey1, d2->pub_key, D);
	len2 = DH_compute_key((unsigned char*)sharekey2, D->pub_key, d2);
	if (len1 != len2) {
		printf("生成共享密钥失败1\n");
		system("pause");
		return -1;
	}
	/*if (memcmp(sharekey1, sharekey2, len1) != 0) {
		printf("生成共享密钥失败2\n");
		system("pause");
		return -1;
	}*/
	printf("生成共享密钥成功\n");
	int j = 0;
	while (sharekey1[j] != '\0') {
		cout << (int)sharekey1[j] << " ";
		j++;
	}
	cout << endl;
	j = 0;
	while (sharekey2[j] != '\0') {
		cout << (int)sharekey2[j] << " ";
		j++;
	}
	//cout << sharekey1[0] << endl << sharekey2[0] << endl;
	system("pause");
	return 0;
}
}

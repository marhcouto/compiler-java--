.class public Arithmetic_less
.super java/lang/Object
.method public <init>()V
   aload_0
   invokenonvirtual java/lang/Object/<init>()V
   return
.end method
.method public static main([Ljava/lang/String;)V
	.limit stack 99
	.limit locals 99
	ldc 10
	ldc 20
	if_icmpge FALSE_1
	iconst_1
	goto TRUE_1
FALSE_1:
	iconst_0
TRUE_1:
	astore 1
	aload 1
	astore 2
	aload 2
	ifeq Else0
	iconst_1
	invokestatic io/print(I)V
	goto EndIf0
Else0:
	iconst_0
	invokestatic io/print(I)V
EndIf0:
	return
.end method
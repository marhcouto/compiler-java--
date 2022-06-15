.class public SimpleWhileStat
.super java/lang/Object


.method public <init>()V
   aload_0
   invokenonvirtual java/lang/Object/<init>()V
   return
.end method
.method public static main([Ljava/lang/String;)V
	.limit stack 2
	.limit locals 6
	iconst_3
	istore_1
	iconst_0
	istore_2
Loop0:
	iload_2
	iload_1
	if_icmplt FALSE0
	iconst_0
	goto TRUE0
FALSE0:
	iconst_1
TRUE0:
	istore_3
	iload_2
	invokestatic ioPlus/printResult(I)V

	iload_2
	iconst_1
	iadd
	istore 5
	iload 5
	istore_2
	iload_3
	ifne Loop0 
	return
.end method

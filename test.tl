def int fib(int n)
{
	if (n <= 2){
		return 1;
	}
	
	return n;
}

local int a;
a = 1;
while(a < 15){
	print("answer: ", fib(a));
	a = a + 1;
}
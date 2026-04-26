struct name {
    int a;
    char b[];
    double c[10];
};

int main() {
    int x; // this is a comment!
    int a;
    int b;
    int c;
    double d;
    double exp;
    char a;
    char escape;

    x=0;
    a = 0123;
    b = 0x20Ab;
    c = -123;
    d = -0.123;
    exp = 123e132;
    a='a';
    escape = '\\';

    a / b;
    a+b;
    a-b;
    a*b;
    a >= b;
    a <= b;
    a < b;
    a>b;
    a!=b;
    a==b;
    !a;
    a&&b;
    a||b;
    print("Hello world!\n");
    return 0;
}
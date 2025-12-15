#include <iostream>
#include <string>
#include <vector>


template<typename T>
void print(const T& value) {
    std::cout << value;
}

template<typename T, typename... Args>
void print(const T& first, const Args&... args) {
    std::cout << first;
    print(args...);
}

template<typename T>
void println(const T& value) {
    std::cout << value << std::endl;
}

template<typename T, typename... Args>
void println(const T& first, const Args&... args) {
    std::cout << first;
    println(args...);
}

void println() {
    std::cout << std::endl;
}
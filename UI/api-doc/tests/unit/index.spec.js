describe("test_jest", () => {
    it("test_func", () => {
        function func() {
            console.log("hello");
        }

        func();

        function func1(a, b) {
            return a + b;
        }

        var result = func1(1, 2);
        expect(result).toBe(3);
    });

    it("test_func1", () => {
        function func() {
            console.log("hello");
        }

        func();

        function func1(a, b) {
            return a + b;
        }

        var result = func1(1, 2);
        expect(result).toBe(4);
    });
});

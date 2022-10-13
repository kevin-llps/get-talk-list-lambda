module.exports = {
    testRegex: "(/src/js/tests/.*|(\\.|/)(test|spec))\\.(jsx?|tsx?)$",
    testPathIgnorePatterns: ["/lib/", "/node_modules/"],
    moduleFileExtensions: ["js", "jsx", "json", "node"],
    collectCoverage: true,
    clearMocks: true
};
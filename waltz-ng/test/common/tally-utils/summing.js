import assert from "assert";
import {buildPropertySummer} from "../../../client/common/tally-utils";


const b21 = {
    id: 221,
    directCount: 2210,
    children: []
};


const b2 = {
    id: 22,
    directCount: 220,
    children: [b21]
};

const b1 = {
    id: 21,
    directCount: 210,
    children: []
};


const b = {
    id: 2,
    directCount: 20,
    children: [b1, b2]
};


const a = {
    id: 1,
    directCount: 10,
    children: []
};


const summer = buildPropertySummer();

describe("basicSumming", () => {
    it("can sum leaf nodes", () => {
        const aTotal = summer(a);
        const b21Total = summer(b21);
        assert.equal(
            aTotal,
            a.directCount,
            "no children therefore only direct count");
        assert.equal(
            b21Total,
            b21.directCount,
            "no children therefore only direct count");
    });

    it("it does something when given null", () => {
        const nullTotal = summer(null);
        assert.equal(
            nullTotal,
            0,
            "total will be zero if given node is null");
    });

    it("it can handle trees of depth 1", () => {
        const b2Total = summer(b2);
        assert.equal(
            b2Total,
            b2.directCount + b21.directCount,
            "total expected to be sum of b2 and b21");
    });

    it("can deal with hierarchies", () => {
        const bTotal = summer(b);
        assert.equal(
            bTotal,
            b.directCount + b1.directCount + b2.directCount + b21.directCount,
            "total expected to be sum of entire b subtree");
    });

    it("mutates the input", () => {
        const bTotal = summer(b);
        assert.equal(
            b.totalCount,
            b.directCount + b1.directCount + b2.directCount + b21.directCount,
            "total expected to be sum of entire b subtree");
        assert.equal(
            b.indirectCount,
            b1.directCount + b2.directCount + b21.directCount,
            "indirect count is cumulative total of children (without self)");
    });
});


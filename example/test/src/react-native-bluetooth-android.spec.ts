import utils from '../../src/libs/utils'

it("hexToBuffer(aabbccdd45)", () => {
    let hex = 'aabbccdd45'
    let buffer = utils.hexToBuffer(hex)
    let res = utils.bufferToHex(buffer)
    expect(hex).toBe(res)
})

it("", () => {
    let str = 'abcfteds'
    let buffer = utils.stringToBuffer(str)
    let res = utils.bufferToString(buffer)
    expect(res).toBe(str)
})
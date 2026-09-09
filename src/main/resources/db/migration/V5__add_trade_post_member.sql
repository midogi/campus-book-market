ALTER TABLE trade_post
    ADD COLUMN member_id BIGINT;

ALTER TABLE trade_post
    ADD CONSTRAINT fk_trade_post_member
        FOREIGN KEY (member_id)
            REFERENCES member_account(id);

CREATE INDEX idx_trade_post_member_id
    ON trade_post(member_id);
